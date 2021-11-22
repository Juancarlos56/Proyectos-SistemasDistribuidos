# serializers.py
from rest_framework import serializers
from .models import *


class CuentaSerializer(serializers.ModelSerializer): 
   
    class Meta: 
        model = Cuenta
        fields = ('cedulaCliente', 'nombreCompletoCliente', 'numeroCuenta','montoCuenta',)
        
class AllTransferenciasSerializer(serializers.ModelSerializer): 
    #cuentasTransferencia = CuentaSerializer(many=True)
    class Meta: 
        model = Transferencia
        fields = ('montoTransferencia', 'tipoTransferencia')

class TransferenciasSerializer(serializers.ModelSerializer): 
    #cuentasTransferencia = CuentaSerializer(many=True)
    class Meta: 
        model = Transferencia
        fields = ('montoTransferencia', 'tipoTransferencia',)
        
class AllTransferenciasCuentasSerializer(serializers.ModelSerializer): 
    cuentasTransferencia = TransferenciasSerializer(many=True)
    class Meta: 
        model = Cuenta
        fields = ('cedulaCliente', 'nombreCompletoCliente', 'numeroCuenta','montoCuenta','cuentasTransferencia')
