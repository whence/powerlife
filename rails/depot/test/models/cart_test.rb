require 'test_helper'

class CartTest < ActiveSupport::TestCase
  test 'adding 2 unique products' do
    cart = carts(:one)
    cart.add_product(products(:ruby)).save!
    cart.add_product(products(:two)).save!
    assert_equal 2, cart.line_items.size
  end

  test 'adding 2 same products' do
    cart = carts(:two)
    cart.add_product(products(:ruby)).save!
    cart.add_product(products(:ruby)).save!
    assert_equal 1, cart.line_items.size
  end
end
