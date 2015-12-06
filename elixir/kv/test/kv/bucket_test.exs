defmodule KV.BucketTest do
  use ExUnit.Case, async: true

  setup do
    {:ok, bucket} = KV.Bucket.start_link
    {:ok, bucket: bucket}
  end

  test "stores value by key", %{bucket: bucket} do
    assert KV.Bucket.get(bucket, "milk") == nil

    KV.Bucket.put(bucket, "milk", 3)
    assert KV.Bucket.get(bucket, "milk") == 3
  end

  test "delete value by key", %{bucket: bucket} do
    KV.Bucket.put(bucket, "wes", "rocks")
    assert KV.Bucket.get(bucket, "wes") == "rocks"

    assert KV.Bucket.delete(bucket, "wes") == "rocks"
    assert KV.Bucket.get(bucket, "wes") == nil
  end
end
