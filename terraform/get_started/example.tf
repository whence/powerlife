provider "aws" {
    region = "${var.region}"
    max_retries = 5
}

resource "aws_instance" "example" {
    ami = "${lookup(var.amis, var.region)}"
    instance_type = "t2.medium"
}
