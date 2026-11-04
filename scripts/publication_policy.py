"""Rules shared by source export and license-aware payload assembly."""
import json,re
AERO_PREFIX='modules/create-aeronautics-adapted-263/'
AERO_JARS={'simulated':'simulated-adapted.jar','offroad':'offroad-adapted.jar','aeronautics':'aeronautics-adapted.jar'}

def restricted_source(name):
    return (name.startswith(AERO_PREFIX) and '/resources/assets/' in name) or ('/libs/' in name and name.endswith('.jar'))

def restricted_payload_entries(lock):
    result={name:set() for name in AERO_JARS.values()}
    for resource in lock['resources']:
        if resource['id']=='aeronautics-assets':
            for target in resource['targets']:
                match=re.fullmatch(re.escape(AERO_PREFIX)+r'(simulated|offroad|aeronautics)/src/(?:main|generated)/resources/(assets/.+)',target['path'])
                if not match:raise ValueError('Unexpected restricted asset mapping: '+target['path'])
                result[AERO_JARS[match[1]]].add(match[2])
        elif resource['id']=='diesel-engine-audio':
            result['diesel-adapted.jar']={'assets/createdieselgenerators/sounds/engine_normal.ogg'}
    return result

def sanitize_editor_metadata(name,data):
    """Remove absolute workstation paths from Blockbench-only metadata."""
    if not name.endswith('.bbmodel'):return data
    value=json.loads(data)
    changed=False
    def clean(node):
        nonlocal changed
        if isinstance(node,dict):
            for key,item in node.items():
                if key in ('path','relative_path') and isinstance(item,str) and re.match(r'^(?:[A-Za-z]:[\\/]|\\\\|/Users/|/home/)',item):
                    node[key]='';changed=True
                else:clean(item)
        elif isinstance(node,list):
            for item in node:clean(item)
    clean(value)
    return (json.dumps(value,ensure_ascii=False,separators=(',',':'))+'\n').encode('utf-8') if changed else data
