require 'term/ansicolor'

index_on = false
working_on = false
ahead = false
behind = false
branch = ''

IO.popen(['git', '-c', 'color.status=false', 'status', '--short', '--branch']) do |io|
    while line = io.gets do
        case line.chomp
        when /^(?<index>[^#])(?<working>.) (?<path1>.*?)(?: -> (?<path2>.*))?$/
            case $~[:index]
            when 'A','M','R','C','D','U'
                index_on = true
            end
            case $~[:working]
            when '?','A','M','D','U'
                working_on = true
            end
        when /^## (?<branch>\S+?)(?:\.\.\.(?<upstream>\S+))?(?: \[(?:ahead (?<ahead>\d+))?(?:, )?(?:behind (?<behind>\d+))?\])?$/
            branch = $~[:branch]
            ahead = (ahead || ($~[:ahead].to_i != 0))
            behind = (behind || ($~[:behind].to_i != 0))
        when /^## Initial commit on (?<branch>\S+)$/
            branch = $~[:branch]
        end
    end
end

print ' ['

branch_summary = if branch == '' then 'unknown' elsif branch.length > 20 then "#{branch[0...20]}.." else branch end
if index_on
    print Term::ANSIColor.yellow(Term::ANSIColor.bold(branch_summary))
else
    print Term::ANSIColor.green(Term::ANSIColor.bold(branch_summary))
end

if working_on || ahead || behind
    suffixes = []
    suffixes << '*' if working_on
    suffixes << '+' if ahead
    suffixes << '-' if behind
    print Term::ANSIColor.yellow(Term::ANSIColor.bold(suffixes.join))
end

print ']'
