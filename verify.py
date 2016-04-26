#!/usr/bin/env python3
# -*- coding: utf-8 -*-
__author__ = 'Terry Wu'
import sys
from PIL import ImageFilter, Image
def detect_gf_result(image_path = 'd:\gf\gf.jpg'):
    img = Image.open(image_path)
    if hasattr(img, "width"):
        width, height = img.width, img.height
    else:
        width, height = img.size
    for x in range(width):
        for y in range(height):
            if img.getpixel((x, y)) < (100, 100, 100):
                img.putpixel((x, y), (256, 256, 256))
    gray = img.convert('L')
    two = gray.point(lambda x: 0 if 68 < x < 90 else 256)
    min_res = two.filter(ImageFilter.MinFilter)
    med_res = min_res.filter(ImageFilter.MedianFilter)
    for _ in range(2):
        min_res = med_res.filter(ImageFilter.MedianFilter)
    min_res.save('d:/gf/gf_bw.jpg','JPEG')
    #print(med_res)

def test():
    args = sys.argv
    if len(args)==1:
            print('Hello, world!')
    elif len(args)==2:
        print('Hello, %s!' % args[1])
    else:
        print('Too many arguments!')

if __name__=='__main__':
    detect_gf_result()