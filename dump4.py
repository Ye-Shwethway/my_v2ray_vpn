import zipfile
import re

with zipfile.ZipFile('classes.jar', 'r') as z:
    data = z.read('libv2ray/CoreCallbackHandler.class')
    strings = re.findall(b'[a-zA-Z0-9_]{3,}', data)
    print(f"--- libv2ray/CoreCallbackHandler.class ---")
    for s in set(strings):
        print(s.decode('utf-8'))
