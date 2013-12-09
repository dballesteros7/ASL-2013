'''
Created on Dec 8, 2013

@author: diegob
'''

import sys

header_blob_1 = """%Training data with country codes
@RELATION training_country

@ATTRIBUTE cityname STRING
@ATTRIBUTE class {196,403,458,564,581,598,630,658,677,758,789,884,897,984,985}

@DATA
"""

def main():
    training_file = open('/home/diegob/workspace/ML-2013-Project-3/data/training.csv', 'r')
    output_file = open('/home/diegob/workspace/ML-2013-Project-3/Diego/training_country.arff', 'w')
    output_file.write(header_blob_1)
    for line in training_file:
        data_tuple = line.strip().split(',')
        city_name = data_tuple[0]
        country_code = data_tuple[2]
        city_name = city_name.replace("'", "\\'")
        output_file.write("'%s',%s\n" % (city_name, country_code))
    output_file.close()
    training_file.close()

if __name__ == '__main__':
    sys.exit(main())