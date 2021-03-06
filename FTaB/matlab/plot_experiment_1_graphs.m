close all;
%% Plot response time, both experimental and theoretical for read operations
responsetime_read = import_statistics_experiment_1('statistics_responsetime_read.csv');

responsetime_averages = zeros(4, 9);
responsetime_stdev = zeros(4,9);
responsetime_percentile_90 = zeros(4,9);

for i = 2:5
    for j = 2:10
        string_value = responsetime_read{i,j};
        string_cells = strsplit(string_value, ':');
        responsetime_averages(i - 1, j - 1) = str2double(string_cells{1});
        responsetime_stdev(i - 1, j - 1) = str2double(string_cells{2});
        responsetime_percentile_90(i - 1, j - 1) = str2double(string_cells{3});
    end
end

throughput_read = import_throughput_experiment_1('throughput_read.csv');

throughput_read_values = zeros(4, 9);
theoretical_response_time_values = zeros(4,9);
x = [5, 10, 20, 30 ,40 ,50 ,70, 80, 100];
think_times = [200, 500, 1000, 2000];
for i = 2:5
    for j = 2:10
        throughput_read_values(i - 1, j - 1) = throughput_read(i,j);
        theoretical_response_time_values(i - 1, j - 1) = 1000*x(j - 1)/throughput_read_values(i - 1, j - 1) - think_times(i - 1);
    end
end

h = line(x, responsetime_averages);
hText =text(x(2),responsetime_averages(1,5),sprintf('(%d, %0.1f)', x(5), responsetime_averages(1,5)));
hLegend = legend(h, '0.2', '0.5', '1', '2');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Experimental response time', 10, 'Read operation']);
hXLabel = xlabel('Number of reader clients (N_{R})');
hYLabel = ylabel('Response time (ms)');
set(hText, 'FontWeight', 'bold');
set([hText, hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hText, hXLabel, hYLabel, v], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:500:2500, ...
  'LineWidth'   , 1         );
set(h(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerSize'      , 8);
set(h(2)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h(4)                        , ...
  'LineWidth'       ,2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 experimental_response_time_read.eps
close;

h = line(x, theoretical_response_time_values);
hText =text(x(2),theoretical_response_time_values(1,5),sprintf('(%d, %0.1f)', x(5), theoretical_response_time_values(1,5)));
hLegend = legend(h, '0.2', '0.5', '1', '2');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Theoretical response time', 10, 'Read operation']);
hXLabel = xlabel('Number of reader clients (N_{R})');
hYLabel = ylabel('Response time (ms)');
set(hText, 'FontWeight', 'bold');
set([hText, hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hText, hXLabel, hYLabel, v], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:500:2500, ...
  'LineWidth'   , 1         );
set(h(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerSize'      , 8);
set(h(2)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h(4)                        , ...
  'LineWidth'       ,2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 theoretical_response_time_read.eps
close;

%% Plot response time, both experimental and theoretical for send operations
responsetime_send = import_statistics_experiment_1('statistics_responsetime_send.csv');

responsetime_averages = zeros(4, 9);
responsetime_stdev = zeros(4,9);
responsetime_percentile_90 = zeros(4,9);

for i = 2:5
    for j = 2:10
        string_value = responsetime_send{i,j};
        string_cells = strsplit(string_value, ':');
        responsetime_averages(i - 1, j - 1) = str2double(string_cells{1});
        responsetime_stdev(i - 1, j - 1) = str2double(string_cells{2});
        responsetime_percentile_90(i - 1, j - 1) = str2double(string_cells{3});
    end
end

throughput_send = import_throughput_experiment_1('throughput_send.csv');

throughput_send_values = zeros(4, 9);
theoretical_response_time_values = zeros(4,9);
x = [5, 10, 20, 30 ,40 ,50 ,70, 80, 100];
think_times = [200, 500, 1000, 2000];
for i = 2:5
    for j = 2:10
        throughput_send_values(i - 1, j - 1) = throughput_send(i,j);
        theoretical_response_time_values(i - 1, j - 1) = 1000*x(j - 1)/throughput_send_values(i - 1, j - 1) - think_times(i - 1);
    end
end

h = line(x, responsetime_averages);
hText =text(x(2),responsetime_averages(1,5),sprintf('(%d, %0.1f)', x(5), responsetime_averages(1,5)));
hLegend = legend(h, '0.2', '0.5', '1', '2');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Experimental response time', 10,'Send operation']);
hXLabel = xlabel('Number of sender clients (N_{S})');
hYLabel = ylabel('Response time (ms)');
set(hText, 'FontWeight', 'bold');
set([hText, hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hText, hXLabel, hYLabel, v], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:500:2500, ...
  'LineWidth'   , 1         );
set(h(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerSize'      , 8);
set(h(2)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h(4)                        , ...
  'LineWidth'       ,2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 experimental_response_time_send.eps
close;

h = line(x, theoretical_response_time_values);
hText =text(x(2),theoretical_response_time_values(1,5),sprintf('(%d, %0.1f)', x(5), theoretical_response_time_values(1,5)));
hLegend = legend(h, '0.2', '0.5', '1', '2');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Theoretical response time', 10, 'Send operation']);
hXLabel = xlabel('Number of sender clients (N_{S})');
hYLabel = ylabel('Response time (ms)');
set(hText, 'FontWeight', 'bold');
set([hText, hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hText, hXLabel, hYLabel, v], 'FontSize', 14);set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:500:2500, ...
  'LineWidth'   , 1         );
set(h(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'o'         , ...
  'MarkerSize'      , 8);
set(h(2)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h(4)                        , ...
  'LineWidth'       ,2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 theoretical_response_time_send.eps
close;

%% Plot service times for read operations
servicetime_read = import_statistics_experiment_1('statistics_servicetime_read.csv');

servicetime_averages = zeros(4, 9);
servicetime_stdev = zeros(4,9);
servicetime_90 = zeros(4,9);

for i = 2:5
    for j = 2:10
        string_value = servicetime_read{i,j};
        string_cells = strsplit(string_value, ':');
        servicetime_averages(i - 1, j - 1) = str2double(string_cells{1});
        servicetime_stdev(i - 1, j - 1) = str2double(string_cells{2});
        servicetime_90(i - 1, j - 1) = str2double(string_cells{3});
    end
end

x = [5, 10, 20, 30 ,40 ,50 ,70, 80, 100];
figure;

h = line(x, servicetime_averages);
hLegend = legend(h, '0.2', '0.5', '1', '2');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Service time', 10, 'Read operation']);
hXLabel = xlabel('Number of reader clients (N_{R})');
hYLabel = ylabel('Service time (ms)');
set([hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hXLabel, hYLabel, v], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,28])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:4:28, ...
  'LineWidth'   , 1         );
set(h(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h(2)                      , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h(4)                       , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 servicetime_read.eps
close;

%% Plot service times for send operations
servicetime_send = import_statistics_experiment_1('statistics_servicetime_send.csv');

servicetime_averages = zeros(4, 9);
servicetime_stdev = zeros(4,9);
servicetime_90 = zeros(4,9);

for i = 2:5
    for j = 2:10
        string_value = servicetime_send{i,j};
        string_cells = strsplit(string_value, ':');
        servicetime_averages(i - 1, j - 1) = str2double(string_cells{1});
        servicetime_stdev(i - 1, j - 1) = str2double(string_cells{2});
        servicetime_90(i - 1, j - 1) = str2double(string_cells{3});
    end
end

x = [5, 10, 20, 30 ,40 ,50 ,70, 80, 100];
figure;

h = line(x, servicetime_averages);
hLegend = legend(h, '0.2', '0.5', '1', '2');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Service time', 10, 'Send operation']);
hXLabel = xlabel('Number of sender clients (N_{S})');
hYLabel = ylabel('Service time (ms)');
set([hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hXLabel, hYLabel, v], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,28])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:4:28, ...
  'LineWidth'   , 1         );
set(h(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h(2)                      , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h(4)                       , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 servicetime_send.eps
close;

%% Plot throughput with bounds for read operations

servicetime_read = import_statistics_experiment_1('statistics_servicetime_read.csv');
throughput_read = import_throughput_experiment_1('throughput_read.csv');
servicetime_averages = zeros(4, 9);
throughput_read_values = zeros(4, 9);
throughput_bounds = zeros(4,9);

for i = 2:5
    for j = 2:10
        string_value = servicetime_read{i,j};
        string_cells = strsplit(string_value, ':');
        servicetime_averages(i - 1, j - 1) = str2double(string_cells{1});
    end
end


x = [5, 10, 20, 30 ,40 ,50 ,70, 80, 100];
think_times = [200, 500, 1000, 2000];
for i = 2:5
    for j = 2:10
        throughput_read_values(i - 1, j - 1) = throughput_read(i,j);
        throughput_bounds(i - 1, j - 1) = 1000*min(1/servicetime_averages(i - 1, j -1), x(j - 1)/(servicetime_averages(i - 1, j - 1) + think_times(i - 1)));
    end
end

h1 = line(x, throughput_read_values);
h2 = line(x, throughput_bounds);

hLegend = legend([h1;h2], '0.2', '0.5', '1', '2','0.2 (Bound)', '0.5 (Bound)', '1 (Bound)', '2 (Bound)');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Bounded throughput', 10, 'Read operation']);
hXLabel = xlabel('Number of reader clients (N_{R})');
hYLabel = ylabel('Throughput (reads/s)');
set([hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hXLabel, hYLabel, v], 'FontSize', 14);
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
  'YTick'       , 0:10:120, ...
  'LineWidth'   , 1         );

set(h1(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h1(2)                      , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h1(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h1(4)                       , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);

set(h2(1)                        , ...
  'LineWidth'       , 2 ,...
  'LineStyle'       , '--',...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h2(2)                      , ...
  'LineWidth'       , 2 ,...
  'LineStyle'       , '--',...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h2(3)                        , ...
  'LineWidth'       , 2 ,...
    'LineStyle'       , '--',...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h2(4)                       , ...
  'LineWidth'       , 2 ,...
    'LineStyle'       , '--',...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 bounded_throughput_read.eps
close;

%% Plot throughput with bounds for send operations

servicetime_send = import_statistics_experiment_1('statistics_servicetime_send.csv');
throughput_send = import_throughput_experiment_1('throughput_send.csv');
servicetime_averages = zeros(4, 9);
throughput_send_values = zeros(4, 9);
throughput_bounds = zeros(4,9);

for i = 2:5
    for j = 2:10
        string_value = servicetime_send{i,j};
        string_cells = strsplit(string_value, ':');
        servicetime_averages(i - 1, j - 1) = str2double(string_cells{1});
    end
end


x = [5, 10, 20, 30 ,40 ,50 ,70, 80, 100];
think_times = [200, 500, 1000, 2000];
for i = 2:5
    for j = 2:10
        throughput_send_values(i - 1, j - 1) = throughput_send(i,j);
        throughput_bounds(i - 1, j - 1) = 1000*min(1/servicetime_averages(i - 1, j -1), x(j - 1)/(servicetime_averages(i - 1, j - 1) + think_times(i - 1)));
    end
end

h1 = line(x, throughput_send_values);
h2 = line(x, throughput_bounds);

hLegend = legend([h1;h2], '0.2', '0.5', '1', '2','0.2 (Bound)', '0.5 (Bound)', '1 (Bound)', '2 (Bound)');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title (['Bounded throughput', 10, 'Send operation']);
hXLabel = xlabel('Number of sender clients (N_{S})');
hYLabel = ylabel('Throughput (sends/s)');
set([hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hXLabel, hYLabel, v], 'FontSize', 14);
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
  'YTick'       , 0:10:120, ...
  'LineWidth'   , 1         );

set(h1(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h1(2)                      , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h1(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h1(4)                       , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);

set(h2(1)                        , ...
  'LineWidth'       , 2 ,...
  'LineStyle'       , '--',...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h2(2)                      , ...
  'LineWidth'       , 2 ,...
  'LineStyle'       , '--',...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h2(3)                        , ...
  'LineWidth'       , 2 ,...
    'LineStyle'       , '--',...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h2(4)                       , ...
  'LineWidth'       , 2 ,...
    'LineStyle'       , '--',...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 bounded_throughput_send.eps
close;

%% Plot overall throughput
figure;
servicetime_read = import_statistics_experiment_1('statistics_servicetime_read.csv');
servicetime_send = import_statistics_experiment_1('statistics_servicetime_send.csv');
throughput_send = import_throughput_experiment_1('throughput_send.csv');
throughput_read = import_throughput_experiment_1('throughput_read.csv');

servicetime_read_averages = zeros(4, 9);
servicetime_send_averages = zeros(4, 9);
servicetime_averages = zeros(4,9);

throughput_values = zeros(4, 9);
throughput_bounds = zeros(4,9);

for i = 2:5
    for j = 2:10
        string_value = servicetime_read{i,j};
        string_cells = strsplit(string_value, ':');
        servicetime_read_averages(i - 1, j - 1) = str2double(string_cells{1});
    end
end

for i = 2:5
    for j = 2:10
        string_value = servicetime_send{i,j};
        string_cells = strsplit(string_value, ':');
        servicetime_send_averages(i - 1, j - 1) = str2double(string_cells{1});
    end
end

for i = 1:4
    for j = 1:9
        servicetime_averages(i,j) = mean([servicetime_send_averages(i,j), servicetime_read_averages(i,j)]);
    end
end


x = [10, 20, 40, 60 ,80 ,100 ,140, 160, 200];
think_times = [200, 500, 1000, 2000];
for i = 2:5
    for j = 2:10
        throughput_values(i - 1, j - 1) = throughput_read(i,j) + throughput_send(i,j);
        throughput_bounds(i - 1, j - 1) = 1000*min(1/servicetime_averages(i - 1, j -1), x(j - 1)/(servicetime_averages(i - 1, j - 1) + think_times(i - 1)));
    end
end

h1 = line(x, throughput_values);
h2 = line(x, throughput_bounds);

hLegend = legend([h1;h2], '0.2', '0.5', '1', '2','0.2 (Bound)', '0.5 (Bound)', '1 (Bound)', '2 (Bound)');
set(hLegend, 'location', 'NorthEastOutside');
v = get(hLegend,'title');
set(v,'string','Think time (s)');
hTitle  = title ('Bounded throughput');
hXLabel = xlabel('Number of clients');
hYLabel = ylabel('Throughput (ops/s)');
set([hTitle, hXLabel, hYLabel, v], 'FontName','Helvetica');
set([hXLabel, hYLabel, v], 'FontSize', 14);
set(hTitle, 'FontSize', 16, 'FontWeight', 'bold');
set(gca, 'FontSize', 12);
ylim([0,100])
set(gca, ...
  'Box'         , 'off'     , ...
  'TickDir'     , 'out'     , ...
  'TickLength'  , [.02 .02] , ...
  'XMinorTick'  , 'on'      , ...
  'YMinorTick'  , 'on'      , ...
  'YGrid'       , 'on'      , ...
  'XColor'      , [.3 .3 .3], ...
  'YColor'      , [.3 .3 .3], ...
  'YTick'       , 0:20:100, ...
  'LineWidth'   , 1         );

set(h1(1)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h1(2)                      , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h1(3)                        , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h1(4)                       , ...
  'LineWidth'       , 2 ,...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);

set(h2(1)                        , ...
  'LineWidth'       , 2 ,...
  'LineStyle'       , '--',...
  'Marker'          , '.'         , ...
  'MarkerSize'      , 8);
set(h2(2)                      , ...
  'LineWidth'       , 2 ,...
  'LineStyle'       , '--',...
  'Marker'          , 'x'         , ...
  'MarkerSize'      , 8);
set(h2(3)                        , ...
  'LineWidth'       , 2 ,...
    'LineStyle'       , '--',...
  'Marker'          , 's'         , ...
  'MarkerSize'      , 8);
set(h2(4)                       , ...
  'LineWidth'       , 2 ,...
    'LineStyle'       , '--',...
  'Marker'          , 'd'         , ...
  'MarkerSize'      , 8);
set(gcf, 'PaperPositionMode', 'auto');
print -depsc2 bounded_throughput_overall.eps
close;
