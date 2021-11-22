from django.db import models

# Create your models here.
class Cuenta(models.Model): 
    id = models.AutoField(primary_key=True, null= False)
    cedulaCliente = models.CharField(max_length=30, blank= False, null= False)
    nombreCompletoCliente = models.CharField(max_length=30, blank= False, null= False)
    numeroCuenta = models.CharField(max_length=20, blank= False, null= False)
    montoCuenta = models.DecimalField(max_digits = 7,decimal_places = 2)
   
class Transferencia(models.Model):
    id = models.AutoField(primary_key=True, null= False)
    montoTransferencia = models.DecimalField(max_digits = 7,decimal_places = 2)
    tipoTransferencia = models.CharField(max_length=30, blank= False, null= False)
    cuenta = models.ForeignKey('Cuenta',  on_delete=models.CASCADE, null=True, blank=True, related_name='cuentasTransferencia')

