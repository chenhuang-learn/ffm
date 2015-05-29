#! /usr/bin/env python
import sys
import random

input_file = sys.argv[1]
output_file = sys.argv[2]

output_handle = open(output_file, 'w')
for line in open(input_file):
    if random.random() < 0.3:
        output_handle.write(line)
output_handle.close()
