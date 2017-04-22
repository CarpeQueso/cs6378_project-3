#!/usr/bin/python

import os
import sys

net_id = "jac161530"
main_class = "Main"
project_path = ""
# Parmas are: net id, hostname, command
cmd_base_start_shell = 'zsh -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no %s@%s %s; $SHELL" &'

num_nodes = 0
request_delay = 0
cs_execution_time = 0
num_requests_to_generate = 0
mutex_algorithm = ""

nodes = []
node_strings = []

def build_java_run_cmd(project_file_location, main_class_name, node_id, hostname, port,
                       request_delay, cs_execution_time, num_requests_to_generate,
                       mutex_algorithm_name, neighbor_string):
    # Params are: project file location, main class name, node id, hostname, port, request delay,
    # critical section execution time, number of requests to generate, mutex algorithm name,
    # neighbor string
    cmd_base_java_run = "java -cp %s/bin %s %d %s %d %d %d %d %s %s"

    return cmd_base_java_run % (project_file_location, main_class_name, node_id, hostname, port,
                                request_delay, cs_execution_time, num_requests_to_generate,
                                mutex_algorithm_name, neighbor_string)

if len(sys.argv) <= 1:
    print "This function takes one argument: the config file to read"
    sys.exit()

# Parse config file
with open(sys.argv[1], "r") as f:
    first_line_params = f.readline().strip().split()
    num_nodes = int(first_line_params[0])
    request_delay = int(first_line_params[1])
    cs_execution_time = int(first_line_params[2])
    num_requests_to_generate = int(first_line_params[3])
    # Second line contains mutual exclusion algorithm name
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

# Start nodes
for node in nodes:
    node_java_run_cmd = build_java_run_cmd(project_path, main_class, node["id"], node["hostname"], node["port"],
                                           request_delay, cs_execution_time, num_requests_to_generate,
                                           mutex_algorithm, neighbor_string)
    os.system(cmd_base_start_shell % (net_id, node["hostname"], node_java_run_cmd))

