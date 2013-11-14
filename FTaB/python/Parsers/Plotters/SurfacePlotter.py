'''
Created on Nov 13, 2013

@author: Diego Ballesteros (diegob)
'''

from mpl_toolkits.mplot3d import Axes3D
from matplotlib.ticker import LinearLocator, FormatStrFormatter
from matplotlib import cm
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = fig.gca(projection='3d')
X = [1, 2, 5, 10, 50]
Y = [100, 50, 20, 10, 2]
Z = [1759, 1053, 758, 804, 654]
ErrorZ = [163, 134, 240, 274, 221]

surf = ax.plot(X, Y, Z, 'ro-')

for i in np.arange(0, len(X)):
    ax.plot([X[i], X[i]], [Y[i], Y[i]], [Z[i]+ErrorZ[i], Z[i]-ErrorZ[i]], "ro-")

Z = [1772, 1213, 778, 824, 1781]
ErrorZ = [158, 168, 249, 270, 430]

surf = ax.plot(X, Y, Z, 'bs-')
for i in np.arange(0, len(X)):
    ax.plot([X[i], X[i]], [Y[i], Y[i]], [Z[i]+ErrorZ[i], Z[i]-ErrorZ[i]], "bs-")


ax.set_xlabel('Worker threads')
ax.set_ylabel('Clients per worker')
ax.set_zlabel('Response time (ms)')
ax.set_title('Minimum/Maximum average response time\nto read a message \n (2 minutes window)')

plt.show()