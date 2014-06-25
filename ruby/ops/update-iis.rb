if ARGV.size != 2
    puts '2 args expected'
    abort
end

appname = "default web site/#{ARGV[0]}/"
path_new = File.expand_path(ARGV[1])
path_appcmd = 'c:/windows/system32/inetsrv/appcmd.exe'

unless File.exist? path_appcmd
    puts 'could not find appcmd'
    abort
end

def get_path path_appcmd, appname
    groups = IO.popen([path_appcmd, 'list', 'vdir', "/app.name:#{appname}"]) { |io|
        regex = /VDIR (?<quote>["']?)(?<name>.+?)\k<quote> \(physicalPath:(?<path>.+)\)/
        regex.match(io.read)
    }
    if $?.success? and groups and groups[:name].casecmp(appname) == 0
        File.expand_path(groups[:path])
    else
        nil
    end
end

def set_path path_appcmd, appname, path_new
    path_new_win = path_new.gsub('/', '\\')
    ret = IO.popen([path_appcmd, 'set', 'vdir', "/vdir.name:#{appname}", "-physicalPath:#{path_new_win}"]) { |io|
        io.read
    }
    $?.success?
end

path_old = get_path path_appcmd, appname
if not path_old
    puts 'failed to get current path'
elsif path_new.casecmp(path_old) == 0
    puts 'no need to change'
else
    updated = set_path path_appcmd, appname, path_new
    if updated 
        path_new_echo = get_path(path_appcmd, appname)
        if path_new_echo.casecmp(path_new) == 0
            puts "ok. updated [#{appname}] from [#{path_old}] to [#{path_new}]"
        else
            puts "updated but with different value. it is [#{path_new_echo}] while should be [#{path_new}]"
        end
    else
        puts 'failed to update with appcmd'
    end
end
