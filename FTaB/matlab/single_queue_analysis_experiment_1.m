close all;

%% Load needed data on response/service times and pre-process
responsetime_read = import_statistics_experiment_1('statistics_responsetime_read.csv');
responsetime_send = import_statistics_experiment_1('statistics_responsetime_send.csv');
responsetime_averages = zeros(4, 9);

for i = 2:5
    for j = 2:10
        string_value_read = responsetime_read{i,j};
        string_cells_read = strsplit(string_value_read, ':');
        string_value_send = responsetime_send{i,j};
        string_cells_send = strsplit(string_value_send, ':');
        responsetime_averages(i - 1, j - 1) = mean([str2double(string_cells_send{1}), str2double(string_cells_read{1})]);
    end
end

servicetime_read = import_statistics_experiment_1('statistics_servicetime_read.csv');
servicetime_send = import_statistics_experiment_1('statistics_servicetime_send.csv');
servicetime_averages = zeros(4, 9);

for i = 2:5
    for j = 2:10
        string_value_read = servicetime_read{i,j};
        string_cells_read = strsplit(string_value_read, ':');
        string_value_send = servicetime_send{i,j};
        string_cells_send = strsplit(string_value_send, ':');
        servicetime_averages(i - 1, j - 1) = mean([str2double(string_cells_send{1}), str2double(string_cells_read{1})]);
    end
end

%% Use the D/M/1 model with Z = 2, plot alongside real data

servicetime_average_z_2 = mean(servicetime_averages(4,:))/1000;
S = servicetime_average_z_2;
Z = 2;
N = [10, 20, 40, 60 ,80 ,100 ,140, 160, 200];
Z_N = Z./N;
mu = 1/S;
varrho = (mu + sqrt(mu^2 - 4*mu*Z_N))/(2*mu);

E_r = S./(1 - varrho);
h1 = line(N, E_r);
h2 = line(N, responsetime_averages(4,:)./1000);

hTitle  = title ('Response time (Model vs Experimental)');
hXLabel = xlabel('Number of clients');
hYLabel = ylabel('Expected response time (s)');
hLegend = legend([h1, h2], 'Model', 'Experimental');
set(hLegend, 'location', 'NorthEast');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,120])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:20:120, ...
  'LineWidth'   , 1         );

set(h1                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(h2                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);


set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 response_time_model_1.eps
close;


%% Use the M/M/1 model with Z = 2, plot alongside real data

servicetime_average_z_2 = mean(servicetime_averages(4,:));
S = servicetime_average_z_2;
Z = 2000;
N = [10, 20, 40, 60 ,80 ,100 ,140, 160, 200];
Z_N = Z./N;
rho = S./Z_N;

E_r = S./(1 - rho);
h1 = line(N, E_r);
h2 = line(N, responsetime_averages(4,:));

hTitle  = title ('Response time (Model vs Experimental)');
hXLabel = xlabel('Number of clients');
hYLabel = ylabel('Expected response time (ms)');
hLegend = legend([h1, h2], 'Model', 'Experimental');
set(hLegend, 'location', 'NorthEast');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,1000])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:200:1000, ...
  'LineWidth'   , 1         );

set(h1                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(h2                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);

set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 response_time_model_2.eps
close;


%% Mean value analysis for Z = 2

servicetime_average_z_2 = mean(servicetime_averages(4,:));
S = servicetime_average_z_2;
Z = 2000;
N = [10, 20, 40, 60 ,80 ,100 ,140, 160, 200];
E_r = zeros(9,1);
E_x = zeros(9,1);

for i = 1:9
    Q = 0;
    E_R = 0;
    X = 0; 
    for j = 1:N(i)
        E_R = S*(1 + Q); 
        X = N(i)/(Z + E_R);
        Q = E_R*X;
    end
    E_r(i) = E_R;
    E_x(i) = X;
end
h1 = line(N, E_r);
h2 = line(N, responsetime_averages(4,:));

hTitle  = title ('Response time (Model vs Experimental)');
hXLabel = xlabel('Number of clients');
hYLabel = ylabel('Expected response time (ms)');
hLegend = legend([h1, h2], 'Model - MVA', 'Experimental');
set(hLegend, 'location', 'NorthEast');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,1000])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:200:1000, ...
  'LineWidth'   , 1         );

set(h1                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(h2                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);

set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 response_time_model_3.eps
close;

%% Mean valued analysis for Z = 0.2

servicetime_average_z_0_2 = mean(servicetime_averages(1,:));
S = servicetime_average_z_0_2;
Z = 200;
N = [10, 20, 40, 60 ,80 ,100 ,140, 160, 200];
E_r = zeros(9,1);
E_x = zeros(9,1);

for i = 1:9
    Q = 0;
    E_R = 0;
    X = 0; 
    for j = 1:N(i)
        E_R = S*(1 + Q); 
        X = N(i)/(Z + E_R);
        Q = E_R*X;
    end
    E_r(i) = E_R;
    E_x(i) = X;
end

h1 = line(N, E_r);
h2 = line(N, responsetime_averages(1,:));

hTitle  = title ('Response time (Model vs Experimental)');
hXLabel = xlabel('Number of clients');
hYLabel = ylabel('Expected response time (ms)');
hLegend = legend([h1, h2], 'Model - MVA', 'Experimental');
set(hLegend, 'location', 'NorthEast');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,2500])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:250:2500, ...
  'LineWidth'   , 1         );

set(h1                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);
set(h2                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);


set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 response_time_model_4.eps
close;

%% Mean value analysis for Z = 2 and complex model, find optimal S_{io}

servicetime_average_z_2 = mean(servicetime_averages(4,:));
S = servicetime_average_z_2;
Z = 2000;
N = [10, 20, 40, 60 ,80 ,100 ,140, 160, 200];
E_r = zeros(9,1);
E_x = zeros(9,1);

S_2 = [0.1, 0.5, 1, 2];
h = zeros(1,4);

for m = 1:4
    for i = 1:9
        S_k = [S_2(m), S];
        Q = [0,0];
        R = [0, 0];
        X = 0; 
        E_R = 0;
        for j = 1:N(i)
            R = S_k.*(1 + Q); 
            E_R = 2*R(1) + R(2);
            X = N(i)/(Z + E_R);
            Q = X*2*R(1) + X*R(2);
        end
        E_r(i) = E_R;
        E_x(i) = X;
    end
    h(m) = line(N, E_r);
end

h2 = line(N, responsetime_averages(4,:));

hTitle  = title ('Response time (Model vs Experimental)');
hXLabel = xlabel('Number of clients');
hYLabel = ylabel('Expected response time (ms)');
hLegend = legend([h, h2], 'S_{io} = 100\mus', 'S_{io} = 500\mus','S_{io} = 1ms','S_{io} = 2ms','Experimental');
set(hLegend, 'location', 'NorthWest');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,1000])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:200:1000, ...
  'LineWidth'   , 1         );

set(h(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(h(2)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(h(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'd'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(h(4)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , '^'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(h2                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);

set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 response_time_model_5.eps
close;

%% Mean value analysis for Z = 0.2s, 0.5s, 1s, 2s and complex model, S_{io} = 500\mus

Z_array = [200, 500, 1000, 2000];
for m = 1:4
    servicetime_average_z = mean(servicetime_averages(m,:));
    S = servicetime_average_z;
    Z = Z_array(m);
    N = [10, 20, 40, 60 ,80 ,100 ,140, 160, 200];
    
    E_r = zeros(9,1);
    E_x = zeros(9,1);
    S_io = 0.5;

    for i = 1:9
        S_k = [S_io, S];
        Q = [0,0];
        R = [0, 0];
        X = 0; 
        E_R = 0;
        for j = 1:N(i)
            R = S_k.*(1 + Q); 
            E_R = 2*R(1) + R(2);
            X = N(i)/(Z + E_R);
            Q = X*2*R(1) + X*R(2);
        end
        E_r(i) = E_R;
        E_x(i) = X;
    end
    h1 = line(N, E_r);
    h2 = line(N, responsetime_averages(m,:));
    hTitle  = title ('Response time (Model vs Experimental)');
    hXLabel = xlabel('Number of clients');
    hYLabel = ylabel('Expected response time (ms)');
    hLegend = legend([h1, h2], 'Model', 'Experimental');
    set(hLegend, 'location', 'NorthWest');
    set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
    set([hXLabel, hYLabel], 'FontSize', 14);
    set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
    set(gca, 'FontSize', 12);
    ylim([0,1000])
    set(gca, ...
      'Box'         , 'off'     , ...
      'TickDir'     , 'out'     , ...
      'TickLength'  , [.02 .02] , ...
      'XMinorTick'  , 'on'      , ...
      'YMinorTick'  , 'on'      , ...
      'YGrid'       , 'on'      , ...
      'XColor'      , [.3 .3 .3], ...
      'YColor'      , [.3 .3 .3], ...
      'YTick'       , 0:200:1000, ...
      'LineWidth'   , 1         );

    set(h1                        , ...
      'LineWidth'       , 2 ,...
      'Marker'          , 'o'         , ...
      'MarkerFaceColor' , [131	139	131]/255, ...
      'MarkerSize'      , 8, ...
      'Color' , [0	201	87]/255);

    set(h2                        , ...
      'LineWidth'       , 2 ,...
      'Marker'          , 'x'         , ...
      'MarkerSize'      , 8);

    set(gcf, 'PaperPositionMode', 'auto');
    print(gcf, '-depsc2', sprintf('response_time_model_%d.eps', m + 5))
    close;
end



