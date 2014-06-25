class Pile
  attr_reader :sample, :remaining

  def initialize(card_factory, card_count)
    @factory = card_factory
    @remaining = card_count
    @sample = card_factory.new
  end

  def empty?
    @remaining <= 0
  end

  def push
    @remaining += 1
  end

  def pop
    fail "#{@sample} pile is empty" if empty?
    @remaining -= 1
    @factory.new
  end

  def to_s
    "#{@sample} [#{@remaining}]"
  end
end
