require 'ffi'

module Win
	extend FFI::Library

	ffi_lib 'user32'
	ffi_convention :stdcall

	HWND_BROADCAST = 65535
	WM_SYSCOMMAND = 274
	SC_MONITORPOWER = 61808
	MONITOR_ON = -1
	MONITOR_OFF = 2
	MONITOR_STANBY = 1

	attach_function :LockWorkStation, [], :bool
	attach_function :PostMessageW, [ :long, :int, :long, :long ], :int
end

Win.LockWorkStation()
sleep 5
Win.PostMessageW(Win::HWND_BROADCAST, Win::WM_SYSCOMMAND, Win::SC_MONITORPOWER, Win::MONITOR_OFF)
