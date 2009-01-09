/*
 * derived from http://sourceforge.net/projects/usblauncher/
 * compile with: gcc -o ctllauncher ctllauncher.c -lusb
 *
 * NexNo's modifications:
 * -removed the m&s missile launcher code -> dream cheeky only version.
 * -added usb-read functionality to read the status of the devices microswitches
 * -added support to send arbitrary commands. (i.e. controlling more then one motor at the same time)
 *
 *---------------------------------------------------------
 * Original comment:
 * ctlmissile.c - simple code to control USB missile launchers.
 * Copyright 2006 James Puderer <jpuderer@littlebox.ca>
 * Copyright 2006 Jonathan McDowell <noodles@earth.li>
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <usb.h>

int debug = 0;

void test_command(struct usb_device *dev, char code){
        usb_dev_handle *launcher;
        char data[8];
        int ret;
	launcher = usb_open(dev);
	if (launcher == NULL) { perror("Unable to open device"); exit(EXIT_FAILURE); }
	usb_detach_kernel_driver_np(launcher, 0);
	usb_detach_kernel_driver_np(launcher, 1);
	ret = usb_set_configuration(launcher, 1);
	if (ret < 0) { perror("Unable to set device configuration"); exit(EXIT_FAILURE); }
	ret = usb_claim_interface(launcher, 0);	
	if (ret < 0) { perror("Unable to claim interface"); exit(EXIT_FAILURE); }
	
	memset(data, 0, 8);
	data[0] = code;

	ret = usb_control_msg(launcher, USB_DT_HID, USB_REQ_SET_CONFIGURATION, USB_RECIP_ENDPOINT, 0, data, 8, 5000);
	printf("returned: %d\n",ret);
	if (ret != 8) { fprintf(stderr, "Error: %s\n", usb_strerror()); exit(EXIT_FAILURE); }
	usb_release_interface(launcher, 0);
	usb_close(launcher);
}

void usb_read(struct usb_device *dev){
        usb_dev_handle *launcher;
        char data[8];
        int ret;
	launcher = usb_open(dev);
	if (launcher == NULL) { perror("Unable to open device"); exit(EXIT_FAILURE); }
	usb_detach_kernel_driver_np(launcher, 0);
	usb_detach_kernel_driver_np(launcher, 1);
	ret = usb_set_configuration(launcher, 1);
	if (ret < 0) { perror("Unable to set device configuration"); exit(EXIT_FAILURE); }
	ret = usb_claim_interface(launcher, 0);	
	if (ret < 0) { perror("Unable to claim interface"); exit(EXIT_FAILURE); }
	
	memset(data, 0, 8);

	ret = usb_interrupt_read(launcher, 1, data, 8, 5000);
	printf("returned: %d\n",ret);
	
	if (ret != 8) { fprintf(stderr, "Error: %s\n", usb_strerror()); exit(EXIT_FAILURE); }
	//printf("read: %u%u%u%u\n",data[0],data[1],data[2],data[3],data[4]);
	printf("%X %X\n",data[1],data[0]);

	usb_release_interface(launcher, 0);
	usb_close(launcher);
}


int main(int argc, char *argv[]){
	struct usb_bus *busses, *bus;
	struct usb_device *dev = NULL;

	usb_init();
	usb_find_busses();
	usb_find_devices();

	busses = usb_get_busses();
	
	for (bus = busses; bus && !dev; bus = bus->next) {
		for (dev = bus->devices; dev; dev = dev->next) {
			if (debug) {
				printf("Checking 0x%04x:0x%04x\n", dev->descriptor.idVendor, dev->descriptor.idProduct);
			}
			if (dev->descriptor.idVendor == 0x1941 && dev->descriptor.idProduct == 0x8021) {
				if (argc != 2)
					usb_read(dev);
				else
					test_command(dev, atoi(argv[1]));		
				break;
			}
		}
	}

	if (!dev) { fprintf(stderr, "Unable to find device.\n"); exit(EXIT_FAILURE); }
	return 0;
}
