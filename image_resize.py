import os, sys
import io
from PIL import Image,ExifTags

def resize(img,size):
    im = Image.open(img)
    width,height = im.size
    resizedWidth,resizedHeight = 600,600
    if width > height:
        print('Width greater than height')
        resizedWidth = resizedHeight * width/height
    else:
        print('Height greater than width')
        resizedHeight = resizedWidth * height/width
    if im._getexif() != None:
        exif=dict((ExifTags.TAGS[k], v) for k, v in im._getexif().items() if k in ExifTags.TAGS)
        print(exif)
        im=im.rotate(270, expand=True)
    print(width,height)
    mysize = (resizedWidth,resizedHeight)
    print(mysize)
    outfile = os.path.splitext(img)[0]+'_resized' + os.path.splitext(img)[1]
    try:
        im.thumbnail(mysize)
        im.save(outfile, "JPEG")
    except IOError:
        print "cannot resize image for ", im

def perform_resize(img_data,fileType):
    with io.BytesIO() as output:
        im = Image.open(io.BytesIO(img_data))
        width,height = im.size
        resizedWidth,resizedHeight = 600,600
        if width > height:
            resizedWidth = resizedHeight * width/height
        else:
            resizedHeight = resizedWidth * height/width
        mysize = (resizedWidth,resizedHeight)
        if im._getexif() != None:
            exif=dict((ExifTags.TAGS[k], v) for k, v in im._getexif().items() if k in ExifTags.TAGS)
            orientation = exif['Orientation']
            rotations = {
                3: Image.ROTATE_180,
                6: Image.ROTATE_270,
                8: Image.ROTATE_90
            }
            if orientation in rotations:
                im = im.transpose(rotations[orientation])
        im.thumbnail(mysize)
        my_type = 'JPEG'
        if fileType == 'PNG':
            my_type = 'PNG'
        im.save(output, my_type)
        return output.getvalue()

if __name__=='__main__':
    if len(sys.argv) < 2:
        print("Usage is : resize_image.py <image_file_path>")
        exit(0)

    #resize(sys.argv[1],600)
    with open(sys.argv[1],"rb") as infile:
        img = sys.argv[1]
        img_data = infile.read()
        my_data = perform_resize(img_data,"JPEG")
        with open(os.path.splitext(img)[0]+'_resized' + os.path.splitext(img)[1],"wb") as outfile:
            outfile.write(my_data)
            
    
