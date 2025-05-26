numero = 9*5+(5*3)
a = 5
b = 10
comp = 10
resultado = 0
bnum = 30
str = "Datos.txt"
correcto = True
def suma(a, b):
    resultado = a+b
suma(a, b)
if resultado>comp:
    with open("Datos.txt", 'w') as f:
        f.write(str("El numero es mayor a 10"))
else:
    print("El numero no es mayor a 10")
with open("Datos.txt", 'r') as f:
    contenido = f.read()
