param([string]$OutputDirectory = (Join-Path $PSScriptRoot 'output'), [string]$Proxy = '')
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Hash-Bytes([byte[]]$Bytes) {
  $algorithm = [Security.Cryptography.SHA256]::Create()
  try { return ([BitConverter]::ToString($algorithm.ComputeHash($Bytes))).Replace('-','').ToLowerInvariant() } finally { $algorithm.Dispose() }
}
function Locked-Download($Resource) {
  $download = Join-Path $script:cache $Resource.id
  if ((Test-Path -LiteralPath $download) -and ((Get-FileHash -LiteralPath $download -Algorithm SHA256).Hash.ToLowerInvariant() -eq $Resource.sha256)) { return $download }
  Write-Host ('Downloading fixed upstream resource: ' + $Resource.id)
  for ($attempt=1; $attempt -le 3; $attempt++) {
    try {
      $request = @{UseBasicParsing=$true; Uri=$Resource.url; OutFile=$download; TimeoutSec=180}
      if ($Proxy) { $request.Proxy=$Proxy }
      Invoke-WebRequest @request
      if ((Get-FileHash -LiteralPath $download -Algorithm SHA256).Hash.ToLowerInvariant() -ne $Resource.sha256) { throw ('Download hash mismatch: ' + $Resource.id) }
      return $download
    } catch {
      if ((Test-Path -LiteralPath $download) -and ((Get-FileHash -LiteralPath $download -Algorithm SHA256).Hash.ToLowerInvariant() -eq $Resource.sha256)) { return $download }
      if ($attempt -eq 3) { throw }
      Write-Host ('Retrying interrupted download: ' + $Resource.id)
    }
  }
}
$output = [IO.Path]::GetFullPath($OutputDirectory)
[IO.Directory]::CreateDirectory($output) | Out-Null
$packManifest = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'modpack/manifest.json') -Raw | ConvertFrom-Json
if ($packManifest.version -notmatch '^[0-9]+\.[0-9]+\.[0-9]+(?:-[a-z0-9.]+)?$') { throw 'Unexpected pack version' }
$result = Join-Path $output ('Create263-Pack-v' + $packManifest.version + '-HMCL.zip')
if (Test-Path -LiteralPath $result) { throw ('Existing output will not be overwritten: ' + $result) }
$cache = Join-Path $PSScriptRoot '.download-cache'
[IO.Directory]::CreateDirectory($cache) | Out-Null
$work = Join-Path $output ('create263-build-' + [Guid]::NewGuid().ToString('N'))
[IO.Directory]::CreateDirectory($work) | Out-Null
$overrides = Join-Path $work 'overrides'
$mods = Join-Path $overrides 'mods'
[IO.Directory]::CreateDirectory($mods) | Out-Null
$expected = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'manifests/payload-sha256.json') -Raw | ConvertFrom-Json
foreach ($property in $expected.PSObject.Properties) {
  if ($property.Name -notmatch '^[a-z0-9_-]+\.jar$') { throw 'Unexpected payload filename' }
  $jar = Join-Path $mods $property.Name
  $sourceJar = Join-Path (Join-Path $PSScriptRoot 'payload/mods') $property.Name
  Copy-Item -LiteralPath $sourceJar -Destination $jar
  if ((Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $property.Value) { throw ('Payload hash mismatch: ' + $property.Name) }
}
$lock = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'manifests/resources-lock.json') -Raw | ConvertFrom-Json
$jarMap = @{'simulated'='simulated-adapted.jar';'offroad'='offroad-adapted.jar';'aeronautics'='aeronautics-adapted.jar'}
foreach ($resource in $lock.resources) {
  if ($resource.id -eq 'sable-native') { continue } # Permitted native binary is already in Sable; source bootstrap downloads it separately.
  $download = Locked-Download $resource
  if ($resource.id -eq 'official-create-assets') {
    $assetDirectory = Join-Path $overrides 'config/create-adapted/external-assets'
    [IO.Directory]::CreateDirectory($assetDirectory) | Out-Null
    Copy-Item -LiteralPath $download -Destination (Join-Path $assetDirectory 'create-1.21.1-6.0.10.jar')
    continue
  }
  $archive = $null
  if ($resource.type -eq 'zip') { $archive = [IO.Compression.ZipFile]::OpenRead($download) }
  $openJars = @{}
  try {
    foreach ($target in $resource.targets) {
      if ($resource.id -eq 'aeronautics-assets') {
        if ($target.path -notmatch '^modules/create-aeronautics-adapted-263/(simulated|offroad|aeronautics)/src/(?:main|generated)/resources/(assets/[^.].*)$') { throw 'Unexpected asset mapping' }
        $jarName = $jarMap[$Matches[1]]; $entryName = $Matches[2]
        $entry = $archive.GetEntry($target.entry)
        if ($null -eq $entry) { throw 'Pinned archive entry missing' }
        $memory = New-Object IO.MemoryStream
        $inputStream = $entry.Open()
        try { $inputStream.CopyTo($memory); $bytes = $memory.ToArray() } finally { $inputStream.Dispose(); $memory.Dispose() }
      } elseif ($resource.id -eq 'diesel-engine-audio') {
        $jarName='diesel-adapted.jar';$entryName='assets/createdieselgenerators/sounds/engine_normal.ogg';$bytes=[IO.File]::ReadAllBytes($download)
      } else { throw ('Unknown resource: ' + $resource.id) }
      if ((Hash-Bytes $bytes) -ne $target.sha256) { throw ('Resource entry hash mismatch: ' + $entryName) }
      if ((($entryName -split '/') -contains '..') -or $entryName.StartsWith('/') -or $entryName.Contains('\')) { throw 'Unsafe resource path' }
      if (-not $openJars.ContainsKey($jarName)) { $openJars[$jarName] = [IO.Compression.ZipFile]::Open((Join-Path $mods $jarName),[IO.Compression.ZipArchiveMode]::Update) }
      $jar = $openJars[$jarName];$old=$jar.GetEntry($entryName)
      if ($null -ne $old) { $old.Delete() }
      $new=$jar.CreateEntry($entryName,[IO.Compression.CompressionLevel]::Optimal);$stream=$new.Open()
      try { $stream.Write($bytes,0,$bytes.Length) } finally { $stream.Dispose() }
    }
  } finally {
    foreach ($jar in $openJars.Values) { $jar.Dispose() }
    if ($null -ne $archive) { $archive.Dispose() }
  }
}
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'modpack/manifest.json') -Destination $work
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'LICENSES') -Destination $overrides -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'THIRD_PARTY_NOTICES.md') -Destination $overrides
[IO.Compression.ZipFile]::CreateFromDirectory($work,$result,[IO.Compression.CompressionLevel]::Optimal,$false)
Write-Host ('HMCL import package created: ' + $result)
Write-Host ('SHA256: ' + (Get-FileHash -LiteralPath $result -Algorithm SHA256).Hash.ToLowerInvariant())
Write-Host 'Import the resulting ZIP as a NEW modpack in HMCL. Select Java 25. Original saves are never copied.'
