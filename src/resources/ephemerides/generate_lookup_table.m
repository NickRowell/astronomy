%% Load SPICE kernels
% DE430.bsp contains Earth ephemeris data.
% naif0010.tls contains leap seconds data for converting UTC to ephemeris
% time.
cspice_furnsh('kernels.furnsh');

%% Set Julian Day numbers

% Initial
jd_min = 2453736.5;  % 2453736.5 = A.D. 2006-Jan-01 00:00:00.00 (CT)
% Final
jd_max = 2455197.5;  % 2455197.5 = A.D. 2010-Jan-01 00:00:00.00 (CT)
% Time step
jd_delta = 1/24;     % Output a value every hour

%% Set output stuff

fileID = fopen('../../../src/resources/ephemerides/earth.txt','w');

fprintf(fileID,'# NOTE: this ephemeris file was generated from the SPICE kernel de430.bsp\n');
fprintf(fileID,'#       using the matlab script data/Ephemerides/Earth/generate_lookup_table.m\n');
fprintf(fileID,'# Julian Day    X [km]          Y [km]           Z [km]\n');

% Format for output numbers
format LONGG

%% Compute table
for jd = jd_min : jd_delta : jd_max
    
    % Generate Julian Day string
    jdstr = strcat(num2str(jd),' JD');
    
    
    % Get corresponding ephemeris time
    et = cspice_str2et(jdstr);

    % Get Earth position
    % Some NAIF ID codes:
    % Solar System Barycentre: 0
    % Earth mass centre:       399
    [pos, lt] = cspice_spkpos('399', et, 'J2000', 'NONE', '0');
    
    % Print out results
    fprintf(fileID,'%f %f %f %f\n',jd,pos(1), pos(2), pos(3));

end



%% Cleanup
fclose(fileID);
cspice_kclear;

%endfunction
