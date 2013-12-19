'''
Created on Dec 18, 2013

@author: diegob
'''
import os
import sys
import tarfile

import numpy


def generate_table_time(root_dir, output_file, client):
    '''
    This methods exports a csv file with the service/response time
    extracted from the tarred files in the given root_dir.

    The output files are stored in the output_file path, if they already exist
    this method will overwrite them without asking!. The files are stored in
    <output_file>_read.csv and <output_file>_send.csv for the two basic
    operations in the system.

    If client is True, then this assumes that the tarred files contain clients
    logs, i.e. clients-0.log files. Otherwise, it looks for server logs in
    rrt0.log.

    The expected format for the tarred files' name is: trace_18_12_<thread-count>_<clients>.tgz.
    Where clients is the number of readers and senders, i.e. the total number of clients is double the
    value in the tarred file name. The thread count is the number of threads in the server.

    The output tables will contain the average value, standard deviation and
    the 90% percentile for the response times found in the logs. The table will
    have the following format:

    The first row has the values for the thread count, and the first column
    has the values for the number of clients. Note that the first cell
    is empty. The value in each position i,j is a tuple with 3 values: average,
    standard deviation, 90% percentile.
    '''
    client_numbers = [250]
    thread_counts = [1, 2, 4, 5, 10, 20, 25, 50]
    result_read_matrix = {}
    result_write_matrix = {}
    for client_number in client_numbers:
        result_read_matrix[client_number] = {}
        result_write_matrix[client_number] = {}
        for thread_count in thread_counts:
            tarred_logs = tarfile.open(os.path.join(root_dir, 'trace_18_12_%s_%s.tgz' % (thread_count, client_number)), mode = 'r:gz')
            if client:
                interesting_log = tarred_logs.extractfile('clients-0.log')
            else:
                interesting_log = tarred_logs.extractfile('rtt0.log')
            full_log = interesting_log.read()
            full_log_lines = full_log.split('\n')[:-1]
            sorted_lines = sorted(full_log_lines, key = lambda x : float(x.split()[2]))
            min_time = float(sorted_lines[0].split()[2]) + 30000
            max_time = float(sorted_lines[-1].split()[2]) - 30000
            useful_values = filter(lambda x: float(x.split()[2]) > min_time and float(x.split()[2]) < max_time, sorted_lines)
            sorted_read_lines = filter(lambda x: x.split()[1] == 'RETRIEVE_MESSAGE', useful_values)
            sorted_write_lines = filter(lambda x: x.split()[1] == 'SEND_MESSAGE', useful_values)
            read_time_values = map(lambda x: float(x.split()[3]), sorted_read_lines)
            write_time_values = map(lambda x: float(x.split()[3]), sorted_write_lines)
            read_time_statistics = (numpy.mean(read_time_values), numpy.std(read_time_values), numpy.percentile(read_time_values, 90))
            write_time_statistics = (numpy.mean(write_time_values), numpy.std(write_time_values), numpy.percentile(write_time_values, 90))
            result_read_matrix[client_number][thread_count] = read_time_statistics
            result_write_matrix[client_number][thread_count] = write_time_statistics

    output_write_file = open('%s_send.csv' % output_file, 'w')
    output_read_file = open('%s_read.csv' % output_file, 'w')
    for client_number in client_numbers:
        output_write_file.write(',%s' % client_number)
        output_read_file.write(',%s' % client_number)
    output_write_file.write('\n')
    output_read_file.write('\n')
    for thread_count in thread_counts:
        output_write_file.write(str(thread_count))
        output_read_file.write(str(thread_count))
        for client_number in client_numbers:
            output_write_file.write(',%s:%s:%s' % result_write_matrix[client_number][thread_count])
            output_read_file.write(',%s:%s:%s' % result_read_matrix[client_number][thread_count])
        output_write_file.write('\n')
        output_read_file.write('\n')
    output_write_file.close()
    output_read_file.close()
    return

def generate_table_throughput(root_dir, output_file):
    '''
    This methods exports a csv file with the throughput
    extracted from the tarred files in the given root_dir.

    The output files are stored in the output_file path, if they already exist
    this method will overwrite them without asking!. The files are stored in
    <output_file>_read.csv and <output_file>_send.csv for the two basic
    operations in the system.

    The expected format for the tarred files' name is: trace_18_12_<thread-count>_<clients>.tgz.
    Where clients is the number of readers and senders, i.e. the total number of clients is double the
    value in the tarred file name. The think time is in seconds.

    The output tables will contain the average value for the throughput found in the logs. 

    The table will have the following format:

    The first row has the values for the thread count, and the first column
    has the values for the number of clients. Note that the first cell
    is empty. The value in each position i,j is a single scalar value.
    '''
    client_numbers = [250]
    thread_counts = [1, 2, 4, 5, 10, 20]
    result_read_matrix = {}
    result_write_matrix = {}
    for client_number in client_numbers:
        result_read_matrix[client_number] = {}
        result_write_matrix[client_number] = {}
        for thread_count in thread_counts:
            tarred_logs = tarfile.open(os.path.join(root_dir, 'trace_18_12_%s_%s.tgz' % (thread_count, client_number)), mode = 'r:gz')
            interesting_log = tarred_logs.extractfile('clients-0.log')
            full_log = interesting_log.read()
            full_log_lines = full_log.split('\n')[:-1]
            sorted_lines = sorted(full_log_lines, key = lambda x : float(x.split()[2]))
            min_time = float(sorted_lines[0].split()[2]) + 30000
            max_time = float(sorted_lines[-1].split()[2]) - 30000
            useful_values = filter(lambda x: float(x.split()[2]) > min_time and float(x.split()[2]) < max_time, sorted_lines)
            sorted_read_lines = filter(lambda x: x.split()[1] == 'RETRIEVE_MESSAGE', useful_values)
            sorted_write_lines = filter(lambda x: x.split()[1] == 'SEND_MESSAGE', useful_values)
            result_read_matrix[client_number][thread_count] = 1000.0*len(sorted_read_lines)/(float(useful_values[-1].split()[2]) - float(useful_values[0].split()[2]))
            result_write_matrix[client_number][thread_count] = 1000.0*len(sorted_write_lines)/(float(useful_values[-1].split()[2]) - float(useful_values[0].split()[2]))

    output_write_file = open('%s_send.csv' % output_file, 'w')
    output_read_file = open('%s_read.csv' % output_file, 'w')
    for client_number in client_numbers:
        output_write_file.write(',%s' % client_number)
        output_read_file.write(',%s' % client_number)
    output_write_file.write('\n')
    output_read_file.write('\n')
    for thread_count in thread_counts:
        output_write_file.write(str(thread_count))
        output_read_file.write(str(thread_count))
        for client_number in client_numbers:
            output_write_file.write(',%s' % result_write_matrix[client_number][thread_count])
            output_read_file.write(',%s' % result_read_matrix[client_number][thread_count])
        output_write_file.write('\n')
        output_read_file.write('\n')
    output_write_file.close()
    output_read_file.close()
    return

def main():
    #generate_table_time('/home/diegob/workspace/data_milestone_2/Experiment_2/clients/', '/home/diegob/workspace/data_milestone_2/Experiment_2/statistics_responsetime_2', True)
    #generate_table_time('/home/diegob/workspace/data_milestone_2/Experiment_2/server/', '/home/diegob/workspace/data_milestone_2/Experiment_2/statistics_servicetime_2', False)
    generate_table_throughput('/home/diegob/workspace/data_milestone_2/Experiment_2/clients/', '/home/diegob/workspace/data_milestone_2/Experiment_2/throughput_2')

if __name__ == '__main__':
    sys.exit(main())