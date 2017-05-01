#!/usr/bin/python

import sys

if len(sys.argv) <= 1:
    print "Params: [filename of output file to verify]"

def line_is_valid(line):
    components = line.split(":")
    if len(components) != 3:
        return False
    if not (components[0].isdigit() and components[1].isdigit()):
        return False
    if components[2] != "enter" and components[2] != "exit":
        return False
    return True

with open(sys.argv[1], "r") as f:
    previous_line = None
    cs_entering_process_id = None
    for line in f:
        line = line.strip()
        # First check for validity of a given line
        if line_is_valid(line):
            components = line.strip().split(":")
            process_id = int(components[0])
            clock_value = int(components[1])
            state = components[2]
            # Given a valid line, check for structure
            if cs_entering_process_id is None:
                if state != "enter":
                    print "Invalid order!"
                    print previous_line
                    print line
                    sys.exit()
                cs_entering_process_id = process_id
            else:
                if state != "exit":
                    print "Invalid order!"
                    print previous_line
                    print line
                    sys.exit()
                if cs_entering_process_id != process_id:
                    print "Id mismatch!"
                    print previous_line
                    print line
                    sys.exit()
                cs_entering_process_id = None
        else:
            print "Invalid line: " + line
            sys.exit()
        previous_line = line

print "Output file valid"

