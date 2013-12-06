'''
Module for a random generator with exponentially distributed numbers.

Created on Dec 5, 2013

@author: diegob
'''

import time
from Milestone2 import Constants
from Milestone2.DistributionGenerator.UniformDistribution import UniformDistribution

class ExponentialDistribution(UniformDistribution):
    '''
    Random generator class that waits random periods exponentially
    distributed.
    '''

    def wait(self, lambd):
        '''
        Wait for a random period, uniformly distributed between a and b.
        '''
        time.sleep(self.randomer.expovariate(Constants.LAMBD))
        