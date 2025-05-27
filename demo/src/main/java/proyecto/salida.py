var = 0
cero = 0
max = 0
continuar = 1
uno = 1
while continuar==uno:
    print("Por favor ingrese un numero")
    var = int(input())
    if var>max:
        max = var
    if var<cero:
        continuar = 0
        print("El numero mayor es:")
        print(max)
