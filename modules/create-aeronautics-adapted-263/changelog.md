# 本地适配记录

## 1.3.0-adapted.1 — 2026-10-24T15:30:36+08:00

- 迁移 Simulated、Offroad、Aeronautics 到 NeoForge 26.3 和本机固定 Create Adapted，连接独立 Sable 原生物理工程。
- 对齐注册、事件、网络、原生事务、组件库存、对接后端、配方及渲染/流体模型。
- 通过独立专服真实组装推进、重载、供气、事务与客户端世界/叶片显示/保存退出；保留未验证功能矩阵。
- 提供本机启动入口、包与SHA清单、复现说明和审查记录；未发布。

## 上游历史记录

Additions
- Add new texture for the Gyroscopic Mechanism
- Add FE transfer capability to Docking Connectors
- Add comparator output for Typewriters when they are in use
- Add config to hide "new ponder!" tooltip
- Add custom name support for Typewriters
- Add statistics to various items
- Add a custom GUI for Steering Wheel when you're wearing goggles - *BlazerBo1*

Changes
- Update to Sable 2.0.0
- Borehead Bearings no longer break blocks with no collision shape
- Prevent fake players from using Plunger Launchers
- Adjust Optical Sensor formatting
- Remove unused block-state in Altitude Sensor
- Velocity Sensor display link readout will no longer be negative
- Reduce Optical Sensor range by 0.5 blocks
- Make Laser Pointers use the center of the entity for their hit location instead of feet location.
- Prevent Swivel Bearings from updating their target angle if they are disassembled
- Balancing changes for hot air gusts
- Propellers no longer push entities that they don't have line of sight of
- Change formatting for ExtraKinetics information to account for localization
- Scale Levitite shader based on gravity strength

Fixes
- Fix hot air physics at high pressure gradients
- Fix redstone inductor charge / discharge speed - *SeppeOngena*
- Fix for the Redstone Inductor model - *Lucky56*
- Fix ServiceLoader crash in large modpacks - *mrosnerr*
- Fix Borehead Bearings stalling when breaking certain blocks
- Fix Borehead Bearings occasionally crashing the client
- Fix Wheel Mount braking not checking redstone from correct direction
- Fix a crash with the Linked Typewriter
- Fix a crash with Springs
- Fix a crash with the Optical Sensor and waterlogged blocks
- Fix Spring shortening / lengthening issues
- Fix CC methods not properly using underlying value box numbers
- Fix airtight tooltip
- Fix client crash pertaining to Levitite catalyzing
- Fix some ExtraKinetics Issues
- Fix Propeller particles
- Fix Spring and Physics Staff rendering with Iris
- Fix packet validation for Physics Staff
- Fix performance degradation with Adjustable Burner sounds
- Fix Honey Glue recipe using `minecraft:honey` instead of `#c:honey`
- Fix Torsion Spring behavior when angle limit is lowered
- Fix Steam Burner crash on servers
- Fix Swivel Bearing constraints not being applied if plate is in world
- Fix Swivel Bearing disassembly when base and plate are on same sub-level
- Fix some special physics actors (Spring, Swivel Bearing, Rope Connector/Winch, Docking Connector) being movable on create contraptions and causing unusual behavior
- Fix Ropes not respecting tile drops gamerule
- Fix Docking Connector inventory implementation not working with new neoforge inventory changes
- Various other small bug-fixes and crash-fixes