"""Generate the small launcher Ponder structure without a world save or external packages."""
import gzip
import struct
from pathlib import Path

def string(value):
    data = value.encode("utf-8")
    return struct.pack(">H", len(data)) + data

def tag(kind, name, payload):
    return bytes([kind]) + string(name) + payload

def integer(value):
    return struct.pack(">i", value)

def ints(values):
    return bytes([3]) + integer(len(values)) + b"".join(map(integer, values))

def compound(values):
    return b"".join(values) + b"\0"

def compounds(values):
    return bytes([10]) + integer(len(values)) + b"".join(values)

def state(name, properties=None):
    fields = [tag(8, "Name", string(name))]
    if properties:
        fields.append(tag(10, "Properties", compound([tag(8, k, string(v)) for k, v in properties.items()])))
    return compound(fields)

palette = [
    state("minecraft:andesite"),
    state("createbigcannons:cannon_mount", {"facing": "north"}),
    state("cbc_at:steel_rocket_pod_breech", {"facing": "west"}),
    state("cbc_at:steel_rocket_pod_rail", {"facing": "west"}),
]
blocks = []
def block(x, y, z, index):
    blocks.append(compound([tag(9, "pos", ints([x,y,z])), tag(3, "state", integer(index))]))
for x in range(5):
    for z in range(5):
        block(x,0,z,0)
block(3,1,2,1)
block(3,3,2,2)
block(2,3,2,3)
block(1,3,2,3)
root = compound([
    tag(3, "DataVersion", integer(3955)),
    tag(9, "size", ints([5,5,5])),
    tag(9, "palette", compounds(palette)),
    tag(9, "blocks", compounds(blocks)),
    tag(9, "entities", compounds([])),
])
output = Path("src/main/resources/assets/cbcatfix/ponder/small_launcher.nbt")
output.parent.mkdir(parents=True, exist_ok=True)
output.write_bytes(gzip.compress(tag(10, "", root), mtime=0))