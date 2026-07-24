from django.contrib import admin
from django.urls import path, include
from django.http import HttpResponse

def health_check(request):
    """Heroku deployment health check endpoint returning plain text 'ok'."""
    return HttpResponse("ok", content_type="text/plain")

urlpatterns = [
    path('admin/', admin.site.urls),
    path('health/', health_check, name='health_check'),
    path('api/', include('api.urls')),
]
