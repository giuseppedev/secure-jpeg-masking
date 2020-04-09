import sys
import os
import base64
import pyexiv2
from PIL import Image
from Crypto.Cipher import AES


def decrypt(secure, passw):
    iv = 8 * b'\0'
    des = DES.new(passw, DES.MODE_OFB, iv)
    dec = des.decrypt(secure)
    return dec


def checkCipher(cipher, mode):
    if cipher == 'des':
        if mode == 'ofb':
            return 0
        else:
            return -3
    else:
        if mode != 'ofb':
            return -4
        return -2

file_name = sys.argv[1]
cipher = sys.argv[2]
mode = sys.argv[3]
password = sys.argv[4]

# Booleano per la gestione dell'eccezzione relativo alla password
flag = False

# Allungo stringa contenente la password in un multiplo di 8 (per DES)
password += ((8 - len(password) % 8) * "0")

decrompressed_path = "/data/imgs/out/decompressed"
secure_path = "/data/imgs/out/secure"

# Apro l'immagine sicura
img_secure = Image.open(secure_path + "/secure.jpg")

# Creo la nuova immagine che andrà nella cartella decompressed
img_decompressed = img_secure.copy()

# Leggo i metadata del file
metadata = pyexiv2.Image(file_name)

# Leggo i segmenti IPTC e Exif
iptc = metadata.read_iptc()
exif = metadata.read_exif()

# Ottengo le informazioni ed la stringa criptatata relatavi ai ROI salvati
rois = iptc["Iptc.Application2.ObjectName"].split("separator")
info = exif["Exif.Photo.UserComment"].split("-")


for count in range(0, len(rois)):

    # Slitto le informazioni
    info_parts = info[count].split(",")
    lenght_roi = info_parts[0]
    x = info_parts[2]
    y = info_parts[3]

    # Decodifico la stringa con dentro i bytes della ROI
    img_decoded = base64.b64decode(rois[count])

    # Decritto il ROI con la password e elimino l'allungamento che avevo fatto in precedenza per effettuare AES
    img_decrypted_decoded = decrypt(img_decoded, password)[:int(lenght_roi)]

    # Decodifico il ROI ottenuto in precedenza
    img_decrypted_decoded = base64.b64decode(img_decrypted_decoded)

    # Creo un file temporaneo relativo al ROI
    with open(decrompressed_path + "/tmp.jpg", "wb") as img:
        img.write(img_decrypted_decoded)
        img.close()

    # Gestione eccezione per la password errata
    try:
        roi = Image.open(decrompressed_path + "/tmp.jpg")

        # Incollo i bytes decoficati delle ROI all'interno dell'immagine decompressa
        img_decompressed.paste(roi, (int(x), int(y)))
    except Exception:
        flag = True;

        # Rimuovo il file temporaneo salvato in precedenza
        os.remove(decrompressed_path + "/tmp.jpg")

if flag is False:
    # Salvo la nuova immagine decompressa
    img_decompressed.save(decrompressed_path + "/decompressed.jpg", format='JPEG', quality=100)

    # Rimuovo il file temporaneo salvato in precedenza
    os.remove(decrompressed_path + "/tmp.jpg")

# Ritorni per vedere se la decompressione ha avuto successo
# 0 decrompessione effettuata con successo
# -1 password per DES errata
# -2 se il cifrario non è corretto
# -3 se la modalità non è corretta
# -4 se sia il cifrario che la modalità non sono corretti
if flag is True:
    print("-1")
else:
    value = checkCipher(cipher, mode)
    if value == -2:
        print("-2")
    elif value == -3:
        print("-3")
    elif value == -4:
        print("-4")
    else:
        print("0")
