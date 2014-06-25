require 'yaml'

class Client
  def ask(req)
    loop do
      puts "#{req[:name]}: #{req[:message]} (#{req[:commands].join(', ')})"

      req[:items].each_with_index do |item, i|
        puts "#{i}. #{item[:name]} [#{item[:selectable]}]"
      end

      command, argument = begin
                            YAML.load(STDIN.gets.chomp).to_a.first
                          rescue
                            puts 'syntax error'
                            next
                          end
      case command
      when 'skip'
        return :skip if req[:commands].include?(:skip)
        puts 'you cannot skip'
        next
      else
        unless req[:commands].any? { |x| x.to_s == command }
          puts "unknown command #{command}"
          next
        end

        indexes = if argument == 'all'
                    (0...req[:items].length).to_a
                  elsif argument.is_a? Array
                    argument.uniq.sort
                  else
                    []
                  end
        indexes.select! { |i| i >= 0 && i < req[:items].length && req[:items][i][:selectable] }
        case msg = validate(req[:items], indexes, req[:limit])
        when :ok
          return [req[:commands].find { |x| x.to_s == command }, indexes]
        else
          puts msg
        end
      end
    end
  end

  private

  def validate(items, indexes, limit)
    limitor, argument = limit
    case limitor
    when :one
      case indexes.length
      when 1
        :ok
      when 0
        'you must select one item'
      else
        'you can only select one item'
      end
    when :one_to_many
      case indexes.length
      when 0
        'you must select at least one item'
      else
        :ok
      end
    when :upto
      if indexes.length <= argument
        :ok
      else
        "you can only select up to #{argument} items"
      end
    when :exactly
      if indexes.length == argument
        :ok
      elsif indexes.length > argument
        "you must select exactly #{argument} items"
      else
        selectable_count = items.count { |x| x[:selectable] }
        if selectable_count < argument
          :ok
        else
          "you must select #{argument} items as much as possible"
        end
      end
    when :unlimited
      :ok
    else
      fail "unknown limitor #{limitor}"
    end
  end
end
