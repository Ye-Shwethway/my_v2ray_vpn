import zipfile

with zipfile.ZipFile('classes.jar', 'r') as z:
    data = z.read('libv2ray/CoreController.class')
    # print raw bytes or some structure to see the method signatures
    # A simple way to extract method signatures:
    import struct
    def parse_cp(data):
        return []
    # Actually, we can use javap on another container via web?
    # No, let's just parse the class file properly
