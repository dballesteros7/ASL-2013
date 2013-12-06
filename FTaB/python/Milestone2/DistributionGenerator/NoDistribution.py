'''
Module for a deterministic generator.

Created on Dec 6, 2013

@author: diegob
'''

import time

from Milestone2 import Constants


class NoDistribution():
    '''
    Deterministic generator class that waits a fixed period.
    '''

    def seed(self, hashable):
        '''
        This is needed, but do nothing.!
        '''
        pass

    def wait(self):
        '''
        Wait for a fixed period.
        '''
        time.sleep(Constants.WAIT_TIME)
