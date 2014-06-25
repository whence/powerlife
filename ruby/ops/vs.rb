require 'term/ansicolor'
include Term::ANSIColor

paths = []

IO.popen(['git', 'ls-files', '*.sln']) do |io|
  while path = io.gets
    paths << path.chomp
  end
end

if paths.empty?
  puts "[#{yellow('WARN')}] No solution file found"
else
  paths.sort! do |a,b|
    [File.basename(a).downcase, a.downcase] <=> [File.basename(b).downcase, b.downcase]
  end

  if ARGV.empty?
    paths.each.with_index(1) do |path, i|
      puts "#{green(bold(i.to_s.rjust(2)))} #{bold(File.basename(path, '.sln'))} #{File.dirname(path)}"
    end
  else
    number = ARGV[0].to_i
    if number <= paths.length and number >= 1
      path = paths[number-1]
      puts "[#{green('OK')}] Opening #{bold(path)}"
      system('start', path)
    else
      puts "[#{red('ERR')}] Number is out of bound"
    end
  end
end
