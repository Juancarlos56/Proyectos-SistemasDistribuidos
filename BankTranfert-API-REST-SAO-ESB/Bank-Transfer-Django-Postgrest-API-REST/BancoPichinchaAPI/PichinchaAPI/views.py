from django.shortcuts import render
from .models import * 
from .serializers import * 
from rest_framework import viewsets
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.db import connection
import json
  
class CuentaViewSet(viewsets.ModelViewSet):
  queryset = Cuenta.objects.all()
  serializer_class = CuentaSerializer

class AllTransferenciasViewSet(viewsets.ModelViewSet):
  queryset = Transferencia.objects.all()
  serializer_class = AllTransferenciasSerializer  



class AllTransferenciasCuentasViewSet(viewsets.ModelViewSet):
  queryset = Cuenta.objects.all()
  serializer_class = AllTransferenciasCuentasSerializer  



class buscarCuentasCedulaViews(APIView):
    def post(self, request):
      cedulaCliente = request.data.get('cedula')
      if cedulaCliente!= "":
        
        with connection.cursor() as cursor:
          query = '''SELECT *
                    FROM PichinchaAPI_cuenta c 
                    WHERE 
                          c.cedulaCliente=%s
                  '''
          cursor.execute(query,[cedulaCliente])
          #fetchall es para listas cursor.fetchall()
          #fetchone es para tomar el ultimo registro 
          registros = cursor.fetchone()
        jsonCuenta = {"cedulaCliente": registros[1], "nombreCompletoCliente": registros[2], "numeroCuenta": registros[3], "montoCuenta": registros[4]}
        serializer = CuentaSerializer(jsonCuenta)
        return Response(serializer.data, status=status.HTTP_200_OK) 
      else:
        return Response({"status": "error", "data: ": cedulaCliente}, status=status.HTTP_400_BAD_REQUEST)
      

class actualizarMontoCuentaUsuario(APIView):
    def post(self, request):
      cedulaCliente = request.data.get('cedula')
      montoCuenta = 0
      try: 
        montoCuenta = float(request.data.get('monto'))
      except:
          return Response({"status": "error monto mal ingresado"}, status=status.HTTP_400_BAD_REQUEST)
      print(">>>>>>>>>>>>>>>>>>>>>>",montoCuenta)
      if cedulaCliente!= "" and montoCuenta > 0.0:
        
        with connection.cursor() as cursor:
          query = '''UPDATE PichinchaAPI_cuenta 
                      SET montoCuenta = %s
                      WHERE 
                          cedulaCliente=%s
                  '''
          cursor.execute(query,[montoCuenta,cedulaCliente])
          
        return Response({"status":"Registro Actualizado"},status=status.HTTP_200_OK) 
      else:
        return Response({"status": "error cedula mal ingresada o monto inferior a 0"}, status=status.HTTP_400_BAD_REQUEST)

class crearTransferenciaCuenta(APIView):
    def post(self, request):
      cedulaCliente = request.data.get('cedula')
      montoCuenta = 0
      tipoTransferencia = request.data.get('tipoTransferencia')
      try: 
        montoCuenta = float(request.data.get('monto'))
      except:
          return Response({"status": "error monto mal ingresado"}, status=status.HTTP_400_BAD_REQUEST)
    
      if cedulaCliente!= "" and montoCuenta > 0.0:
        
        with connection.cursor() as cursor:
          query = '''SELECT c.id
                    FROM PichinchaAPI_cuenta c 
                    WHERE 
                          c.cedulaCliente=%s
                  '''
          cursor.execute(query,[cedulaCliente])
          idCuenta = cursor.fetchone()
          print(idCuenta)
          query = '''INSERT INTO PichinchaAPI_transferencia (montoTransferencia,tipoTransferencia,cuenta_id) 
                      VALUES (%s, %s, %s)
                  '''
          cursor.execute(query,[montoCuenta, tipoTransferencia, idCuenta[0]])
         
        return Response({"status":"Transferencia Realizada"},status=status.HTTP_200_OK) 
      else:
        return Response({"status": "error cedula mal ingresada o monto inferior a 0"}, status=status.HTTP_400_BAD_REQUEST)