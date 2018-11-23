import sys
import requests
import base64
import json
import os

def resize_image(image):
    with open(image,"rb") as infile:
        img_data = infile.read()
        res = requests.post('http://localhost:8888/image',json.dumps({'data':base64.b64encode(img_data),'fileName':image}))
        resized_data = base64.b64decode(res.json()['data'])
        with open(os.path.splitext(image)[0]+'_resized' + os.path.splitext(image)[1],"wb") as outfile:
            outfile.write(resized_data)


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage is: test_client.py <image_file_path>')
        exit
    resize_image(sys.argv[1])
    
