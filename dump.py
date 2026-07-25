import zipfile
import re
import sys

with zipfile.ZipFile('classes.jar', 'r') as z:
    for name in z.namelist():
        if name.endswith('Libv2ray.class'):
            data = z.read(name)
            # Find printable strings in the class file
            strings = re.findall(b'[a-zA-Z0-9_]{3,}', data)
            print(f"--- {name} ---")
            for s in strings:
                print(s.decode('utf-8'))
