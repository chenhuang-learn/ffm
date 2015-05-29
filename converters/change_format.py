#! /usr/bin/env python
import sys

input_file = sys.argv[1]
output_file = sys.argv[2]

output_handle = open(output_file, 'w')
for line in open(input_file):
    line = line.strip()
    fields = line.split()
    output_line = fields[0]
    for i, field in enumerate(fields[1:], start=1):
        output_line += " " + str(i) + ":" + field + ":1"
    output_line += "\n"
    output_handle.write(output_line)
output_handle.close()

