'''
Created on Nov 13, 2013

@author: Diego Ballesteros (diegob)
'''
from numpy.lib.polynomial import polyfit, poly1d

import matplotlib.pyplot as plt
import numpy as np


x = [1, 10]
y = [687.2, 652.7]
error = [ 1271.2, 259.5]
error_low = []
for idx, z in enumerate(error):
    if z < y[idx]:
        error_low.append(z)
    else:
        error_low.append(y[idx])
marker = 'bo-'


p1, = plt.plot(x,y, marker)
ax = plt.gca()
ax.errorbar(x, y, yerr=np.vstack([error_low, error]))#, marker = marker)

y = [650.5, 666.8]
error = [1200.4, 257.5]
error_low = []
for idx, z in enumerate(error):
    if z < y[idx]:
        error_low.append(z)
    else:
        error_low.append(y[idx])
marker= 'rs-'
p2, = plt.plot(x,y, marker)
ax.errorbar(x, y, yerr=np.vstack([error_low, error]))#, marker = marker)



#ax.set_ylim([-10, 2200])
ax.set_xlim([0, 15])
ax.set_xlabel('Number of queues')
ax.set_ylabel('Average response time (ms)')
ax.set_title('Average response time to send/read a message vs number of target queues\n (250 reader and sender clients, 2000 mesage size)')
plt.legend([p1,p2], ["Read", "Send"])

plt.show()