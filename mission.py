"""
  Crazyflie Mission Script — Auto-generated
  Waypoints : 10
  Velocity  : 0.5 m/s
"""

import time
import cflib.crtp
from cflib.crazyflie import Crazyflie
from cflib.crazyflie.syncCrazyflie import SyncCrazyflie
from cflib.positioning.position_hl_commander import PositionHlCommander

URI = 'radio://0/80/2M/E7E7E7E7E7'

WAYPOINTS = [
    (1,170, 1,370, 1,000),
    (-1,590, 1,600, 1,000),
    (-1,230, -0,580, 1,000),
    (0,980, -0,880, 1,000),
    (1,290, -1,490, 1,000),
    (-1,920, -1,760, 1,000),
    (-2,410, -1,010, 1,000),
    (-2,300, -0,120, 1,000),
    (-2,300, 0,660, 1,000),
    (-3,000, 2,000, 1,000),
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
