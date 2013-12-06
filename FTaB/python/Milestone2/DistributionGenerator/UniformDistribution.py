'''
Module for a random generator with uniformly distributed numbers.

Created on Dec 5, 2013

@author: diegob
'''

import random
import time

from Milestone2 import Constants


class UniformDistribution():
    '''
    Random generator class that waits random periods uniformly
    distributed.
    '''

    def __init__(self):
        '''
        Initializes the generator with its own instance of Random
        '''
        self.randomer = random.Random()

    def seed(self, hashable):
        '''
        Seeds the underlying random generator module
        '''
        self.randomer.seed(hashable)
    
    def wait(self):
        '''
        Wait for a random period, uniformly distributed between a and b.
        '''
        time.sleep(self.randomer.uniform(Constants.MIN_UNIFORM, Constants.MAX_UNIFORM))
