import sys
import os
import base64
import pyexiv2
from Crypto.Cipher import AES


def encrypt(msg, passw):
    iv = 16 * b'\0'
    aes = AES.new(passw, AES.MODE_CFB, iv)
    encd = aes.encrypt(msg)
    return encd


def extract_coords_for_index(index, coords):
    rois = coords.split('-')
    for roi in rois:
        id_roi = roi.split(',')[0]
        if id_roi.isdigit():
            if id_roi == index:
                return roi
    return ""


file_name = sys.argv[1]
coords = sys.argv[2]
password = sys.argv[3]

# Allungo stringa contenente la password in un multiplo di 16 (per AES)
password += ((16 - len(password) % 16) * "0")

roi_path = "/data/imgs/out/roi"

# Leggo i metadata del file
metadata = pyexiv2.Image(file_name)

# Pulizia dei segmenti IPTC e Exif
metadata.clear_iptc()
metadata.clear_exif()

# Liste per le informaizoni da concatenare come metadati
list_img_encrypted_stringed = []
list_new_info = []

# Aggiungo le informazioni all'immagine sicura per ogni roi
for file_roi in os.listdir(roi_path):
    parts = file_roi.split('.')
    index = parts[0]
    with open(roi_path + "/" + file_roi, "rb") as roi:
        # Estraggo le informazioni per ogni roi con l'indice fissato
        info = extract_coords_for_index(index, coords).split(',')
        id = info[0]
        x = info[1]
        y = info[2]

        # Codifico l'immagine in una stringa
        img_stringed = base64.b64encode(roi.read())
        img_decoded = img_stringed.decode('utf-8')

        len_img_decoded = len(img_decoded)
        # Formatto la stringa contenti le informazioni dei ROI da inserire poi nei metadata (Exif) nel formato: Lunghezza,ID,X,Y
        new_info = str(len_img_decoded) + "," + str(id) + "," + str(x) + "," + str(y)

        # Allungo stringa contenente i bytes della ROI in un multiplo di 16 (per AES)
        img_decoded += ((16 - len(img_decoded) % 16) * "0")

        # Cripto la stringa contenente i bytes della ROI con AES
        img_encrypted = encrypt(img_decoded, password)

        # Codifico la stringa criptata appena ottenuta
        img_encrypted_stringed = str(base64.b64encode(img_encrypted), 'utf-8')

        # Aggiungo la strina criptata di ogni ROI alla lista
        list_img_encrypted_stringed.append(img_encrypted_stringed)

        # Aggiungo le informazioni di ogni ROI alla lista
        list_new_info.append(new_info)

# Usiamo la parola 'separator 'per dividere le stringhe criptate di ogni ROI
img_encrypted_stringed = 'separator'.join(list_img_encrypted_stringed)

# Usiamo il carattere '-'per dividere le informazioni di ogni ROI
new_info = '-'.join(list_new_info)

# Aggiungo le informazioni ai metadati
# La stringa criptata la aggiungo al segmento IPTC
metadata.modify_iptc({"Iptc.Application2.ObjectName": img_encrypted_stringed})

# Le informazioni di ogni ROI le aggiungo al segmento Exif nello specifico nella sezione relative ai Commenti
metadata.modify_exif({"Exif.Photo.UserComment": new_info})

# Ritorni per vedere se la modifica dei segmenti è andata a buon fine
# 0 modifica effettuata con successo
# -1 modifica iptc fallita
# -2 modifica exif fallita
# -3 modifica iptc ed exif fallita
# error per indicare un errore sconosciuto
# Controllo se > 2 di perchè quando sono vuoti i segementi contengono solo i due caratteri "{}"
if len(str(metadata.read_iptc())) > 2:
    if len(str(metadata.read_exif())) > 2:
        print("0")
    else:
        print("-2")
else:
    if len(str(metadata.read_iptc())) < 3 and len(str(metadata.read_exif())) < 3:
        print("-3")
    elif len(str(metadata.read_iptc())) < 3:
        print("-1")
    else:
        print("error")
