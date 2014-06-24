module Utils
  module_function

  def find_all_indexes(items)
    items.each_index.select { |i| yield items[i] }
  end
end
