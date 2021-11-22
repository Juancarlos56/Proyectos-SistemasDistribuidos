from django.urls import include, path
from rest_framework import routers
from .views import *

router = routers.DefaultRouter()
router.register(r'transferencias', AllTransferenciasViewSet)
router.register(r'cuentasBanco', CuentaViewSet)
router.register(r'TransferenciasCuentas', AllTransferenciasCuentasViewSet)



urlpatterns = [
    path('api/allAPI/', include(router.urls)),
    path('api/buscarCedula/', buscarCuentasCedulaViews.as_view()),
    path('api/actulizarMontoCuenta/', actualizarMontoCuentaUsuario.as_view()),
    path('api/crearTransferenciaCuenta/', crearTransferenciaCuenta.as_view()),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework'))
]