'''
Created on Nov 13, 2013

@author: Diego Ballesteros (diegob)
'''
from numpy.lib.polynomial import polyfit, poly1d

import matplotlib.pyplot as plt
import numpy as np


x = [10, 12, 16, 25, 50]
y = [39.1759412212, 38.2569129843, 39.6075519783,39.1187951817, 78.2106472431]
error = [ 87.251101961, 74.4513752202,88.3687896286,89.8709845297, 169.655824034,  ]
error_low = []
for idx, z in enumerate(error):
    if z < y[idx]:
        error_low.append(z)
    else:
        error_low.append(y[idx])
marker = 'bo--'


fit = polyfit(x,y,1)
fit_fn = poly1d(fit)
p1, = plt.plot(x,y, marker)
p2, = plt.plot(x, fit_fn(x), 'rs-')
ax = plt.gca()
ax.errorbar(x, y, yerr=np.vstack([error_low, error]))#, marker = marker)
ax.set_ylim([-10, 400])
#ax.set_xlim([0, 7400])
ax.set_xlabel('Total number of readers')
ax.set_ylabel('Average response time (ms)')
ax.set_title('Average response time to read a message vs number of readers in the system')

plt.legend([p2], ["RT(c) = %f*c + %f" % (fit[0], fit[1])])
plt.show()