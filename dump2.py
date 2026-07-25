import zipfile
import re

with zipfile.ZipFile('classes.jar', 'r') as z:
    for name in ['libv2ray/CoreController.class']:
        data = z.read(name)
        strings = re.findall(b'[a-zA-Z0-9_]{3,}', data)
        print(f"--- {name} ---")
        for s in set(strings):
            print(s.decode('utf-8'))
