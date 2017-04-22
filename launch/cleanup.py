#!/usr/bin/python

import os
import sys

net_id = "jac161530"
# Parmas are: net id, hostname, command
cmd_base_kill = 'zsh -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no %s@%s killall -u %s; $SHELL" &'

num_nodes = 0

nodes = []
node_strings = []

if len(sys.argv) <= 1:
    print "This function takes one argument: the config file to read"
    sys.exit()

# Parse config file
with open(sys.argv[1], "r") as f:
    first_line_params = f.readline().strip().split()
    num_nodes = int(first_line_params[0])
    # Unused, here
    mutex_algorithm = f.readline().strip()

    for line in f:
        line = line.strip()
        if len(line) == 0 or line[0] == '#' or not line[0].isdigit():
            continue
        node_params = line.split()
        nodes.append({
            "id": int(node_params[0]),
            "hostname": node_params[1],
            "port": int(node_params[2])
        })
        node_strings.append(":".join(node_params))

neighbor_string = ",".join(node_strings)

for node in nodes:
    os.system(cmd_base_kill % (net_id, node["hostname"], net_id))

