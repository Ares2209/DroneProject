"""
  Crazyflie Mission Script — Auto-generated
  Waypoints : 13
  Velocity  : 0.5 m/s
"""

import time
import cflib.crtp
from cflib.crazyflie import Crazyflie
from cflib.crazyflie.syncCrazyflie import SyncCrazyflie
from cflib.positioning.position_hl_commander import PositionHlCommander

URI = 'radio://0/80/2M/E7E7E7E7E7'

WAYPOINTS = [
    (0,010, -0,000, 1,000),
    (-1,180, 1,170, 1,000),
    (-2,040, 0,220, 1,000),
    (-2,050, -1,500, 1,000),
    (0,340, -1,660, 1,000),
    (2,570, -1,040, 1,000),
    (2,290, 0,430, 1,000),
    (0,570, 1,320, 1,000),
    (0,680, -0,030, 1,000),
    (0,560, -0,620, 1,000),
    (-0,420, -1,020, 1,000),
    (-1,140, -0,890, 1,000),
    (-1,520, -0,170, 1,000),
]

def run_mission(scf):
    with PositionHlCommander(
            scf,
            default_velocity=0.5,
            default_height=1.0
    ) as pc:
        time.sleep(1.0)  # stabilise after takeoff

        for i, (x, y, z) in enumerate(WAYPOINTS):
            print(f'Going to waypoint {i + 1}/{len(WAYPOINTS)}: x={x}, y={y}, z={z}')
            pc.go_to(x, y, z)
            time.sleep(0.5)

        print('Mission complete. Landing...')

if __name__ == '__main__':
    cflib.crtp.init_drivers()
    with SyncCrazyflie(URI, cf=Crazyflie(rw_cache='./cache')) as scf:
        run_mission(scf)
