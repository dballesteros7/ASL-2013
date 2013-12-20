%% Load needed data on response/service times and pre-process
responsetime_read = import_statistics_experiment_1('statistics_responsetime_read.csv');
responsetime_send = import_statistics_experiment_1('statistics_responsetime_send.csv');
responsetime_averages = zeros(4, 4);

for i = 2:5
    for j = 2:5
        string_value_read = responsetime_read{i,j};
        string_cells_read = strsplit(string_value_read, ':');
        string_value_send = responsetime_send{i,j};
        string_cells_send = strsplit(string_value_send, ':');
        responsetime_averages(i - 1, j - 1) = mean([str2double(string_cells_send{1}), str2double(string_cells_read{1})]);
    end
end

servicetime_read = import_statistics_experiment_1('statistics_servicetime_read.csv');
servicetime_send = import_statistics_experiment_1('statistics_servicetime_send.csv');
servicetime_averages = zeros(4, 4);

for i = 2:5
    for j = 2:5
        string_value_read = servicetime_read{i,j};
        string_cells_read = strsplit(string_value_read, ':');
        string_value_send = servicetime_send{i,j};
        string_cells_send = strsplit(string_value_send, ':');
        servicetime_averages(i - 1, j - 1) = mean([str2double(string_cells_send{1}), str2double(string_cells_read{1})]);
    end
end

throughput_read = import_throughput_experiment_1('throughput_read.csv');
throughput_send = import_throughput_experiment_1('throughput_send.csv');
throughput_total = zeros(4, 4);

for i = 2:5
    for j = 2:5
        throughput_total(i - 1, j - 1) = throughput_read(i,j) + throughput_send(i,j);
    end
end

responsetime_read_2 = import_statistics_experiment_1('statistics_responsetime_2_read.csv');
responsetime_send_2 = import_statistics_experiment_1('statistics_responsetime_2_send.csv');
responsetime_averages_2 = zeros(8, 1);

for i = 2:9
    for j = 2:2
        string_value_read = responsetime_read_2{i,j};
        string_cells_read = strsplit(string_value_read, ':');
        string_value_send = responsetime_send_2{i,j};
        string_cells_send = strsplit(string_value_send, ':');
        responsetime_averages_2(i - 1, j - 1) = mean([str2double(string_cells_send{1}), str2double(string_cells_read{1})]);
    end
end

throughput_read_2 = import_throughput_experiment_1('throughput_2_read.csv');
throughput_send_2 = import_throughput_experiment_1('throughput_2_send.csv');
throughput_total_2 = zeros(8, 1);

for i = 2:9
    for j = 2:2
        throughput_total_2(i - 1, j - 1) = throughput_read_2(i,j) + throughput_send_2(i,j);
    end
end

%% Mean value analysis for using optimal parameters from single queue analysis (i.e. S_io = 500\mus)

T = [2, 5, 10, 20];

for m = 1:4
    figure;
    servicetime_average_t_20 = mean(servicetime_averages(m,:));
    S = servicetime_average_t_20;
    Z = 1000;
    N = [40, 80, 120, 200]/T(m);
    E_r = zeros(4,1);
    E_x = zeros(4,1);

    S_io = 0.5;
    for i = 1:4
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
    hXLabel = xlabel('Number of clients per thread');
    hYLabel = ylabel('Expected response time (ms)');
    hLegend = legend([h1, h2], 'Model','Experimental');
    set(hLegend, 'location', 'NorthWest');
    set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
    set([hXLabel, hYLabel], 'FontSize', 14);
    set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
    set(gca, 'FontSize', 12);
    ylim([0,800])
    set(gca, ...
      'Box'         , 'off'     , ...
      'TickDir'     , 'out'     , ...
      'TickLength'  , [.02 .02] , ...
      'XMinorTick'  , 'on'      , ...
      'YMinorTick'  , 'on'      , ...
      'YGrid'       , 'on'      , ...
      'XColor'      , [.3 .3 .3], ...
      'YColor'      , [.3 .3 .3], ...
      'YTick'       , 0:50:800, ...
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
    print(gcf, '-depsc2', sprintf('exp2_response_time_model_1_%d.eps', m))
    close;
    figure;
    h1 = line(N*T(m), 1000*T(m)*E_x);
    h2 = line(N*T(m), throughput_total(m,:));

    hTitle  = title ('Throughput (Model vs Experimental)');
    hXLabel = xlabel('Number of clients');
    hYLabel = ylabel('Throughput (ops/s)');
    hLegend = legend([h1, h2], 'Model','Experimental');
    set(hLegend, 'location', 'NorthWest');
    set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
    set([hXLabel, hYLabel], 'FontSize', 14);
    set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
    set(gca, 'FontSize', 12);
    ylim([0,200])
    set(gca, ...
      'Box'         , 'off'     , ...
      'TickDir'     , 'out'     , ...
      'TickLength'  , [.02 .02] , ...
      'XMinorTick'  , 'on'      , ...
      'YMinorTick'  , 'on'      , ...
      'YGrid'       , 'on'      , ...
      'XColor'      , [.3 .3 .3], ...
      'YColor'      , [.3 .3 .3], ...
      'YTick'       , 0:20:200, ...
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
    print(gcf, '-depsc2', sprintf('exp2_throughput_model_1_%d.eps', m))
    close;
end

%% Linear regression on service time for T = 5

T = [2, 5, 10, 20];

% First define a quadratic regression for the S_io

Z = 1000;
N = [40, 80, 120, 200]/T(2);
service_time_model = fit(T(2)*N(1:3)', servicetime_averages(2,1:3)', 'poly2');
S = [servicetime_averages(2,1:3), service_time_model.p1*200*200 + service_time_model.p2*200 + service_time_model.p3];
E_r = zeros(4,1);
E_x = zeros(4,1);

S_io = 0.5;

for i = 1:4
    S_k = [S_io, S(i)];
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
h2 = line(N, responsetime_averages(2,:));

hTitle  = title ('Response time (Model vs Experimental)');
hXLabel = xlabel('Number of clients per thread');
hYLabel = ylabel('Expected response time (ms)');
hLegend = legend([h1, h2], 'Model','Experimental');
set(hLegend, 'location', 'NorthWest');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,800])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:50:800, ...
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
print(gcf, '-depsc2', 'exp2_response_time_model_2.eps')
close;

h1 = line(N*T(2), 1000*T(2)*E_x);
h2 = line(N*T(2), throughput_total(2,:));

hTitle  = title ('Throughput (Model vs Experimental)');
hXLabel = xlabel('Number of clients');
hYLabel = ylabel('Throughput (ops/s)');
hLegend = legend([h1, h2], 'Model','Experimental');
set(hLegend, 'location', 'NorthWest');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,200])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:20:200, ...
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
print(gcf, '-depsc2', 'exp2_throughput_model_2.eps')
close;


%% Expectations for N = 200

T = [1, 2, 4, 5, 10, 20, 40, 50, 100, 200];

servicetime_average_t_20 = mean(mean(servicetime_averages));
S = servicetime_average_t_20;
Z = 1000;
N = 200./T;
E_r = zeros(10,1);
E_x = zeros(10,1);

S_io = 0.5;
for i = 1:10
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
    E_x(i) = T(i)*1000*X;
end

h1 = line(T, E_r);
hTitle  = title ('Model response time (N = 200)');
hXLabel = xlabel('Number of threads');
hYLabel = ylabel('Expected response time (ms)');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,1100])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:100:1100, ...
  'LineWidth'   , 1         );

set(h1                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(gcf, 'PaperPositionMode', 'auto');
print(gcf, '-depsc2', 'exp2_response_time_model_3_1.eps')
close;

h1 = line(T, E_x);

hTitle  = title ('Model throughput (N = 200)');
hXLabel = xlabel('Number of worker threads');
hYLabel = ylabel('Throughput (ops/s)');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,200])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:20:200, ...
  'LineWidth'   , 1         );

set(h1                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerFaceColor' , [131	139	131]/255, ...
  'MarkerSize'      , 8, ...
  'Color' , [0	201	87]/255);

set(gcf, 'PaperPositionMode', 'auto');
print(gcf, '-depsc2', 'exp2_throughput_model_3_1.eps')
close;

%% Expectations for N = 500

T = [1, 2, 4, 5, 10, 20, 25, 50];

servicetime_average_t_20 = mean(mean(servicetime_averages));
S = servicetime_average_t_20;
Z = 1000;
N = 500./T;
E_r = zeros(8,1);
E_x = zeros(8,1);

S_io = 0.5;
for i = 1:8
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
    E_x(i) = T(i)*1000*X;
end

h1 = line(T, E_r);
h2 = line(T, responsetime_averages_2);
hLegend = legend([h1, h2], 'Model','Experimental');
set(hLegend, 'location', 'NorthWest');
hTitle  = title ('Model and experimental response time (N = 500)');
hXLabel = xlabel('Number of threads');
hYLabel = ylabel('Expected response time (ms)');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,2000])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:200:2000, ...
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
print(gcf, '-depsc2', 'exp2_response_time_model_3_2.eps')
close;
%figure;
h1 = line(T, E_x);
h2 = line(T, throughput_total_2);
hLegend = legend([h1, h2], 'Model','Experimental');
set(hLegend, 'location', 'NorthWest');
hTitle  = title ('Model and experimental throughput (N = 500)');
hXLabel = xlabel('Number of worker threads');
hYLabel = ylabel('Throughput (ops/s)');
set([hTitle, hXLabel, hYLabel], 'FontName','Helvetica');
set([hXLabel, hYLabel], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,500])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:50:500, ...
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
print(gcf, '-depsc2', 'exp2_throughput_model_3_2.eps')
close;
