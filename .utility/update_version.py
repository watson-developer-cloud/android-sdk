#!/usr/bin/env python

import json
import sys

filename = sys.argv[1] + '/.utility/descriptor.json'
new_version = {
    'version': {
        'name': sys.argv[2]
    }
}

with open(filename, 'r') as file:
    data = json.load(file)

data.update(new_version)

with open(filename, 'w') as file:
    file.write(json.dumps(data))