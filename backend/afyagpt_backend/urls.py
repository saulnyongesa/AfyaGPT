from django.contrib import admin
from django.urls import path, include
from django.http import HttpResponse
from api.views import landing_page, stakeholder_dashboard, stakeholder_login, stakeholder_logout, web_register

def health_check(request):
    """Heroku deployment health check endpoint returning plain text 'ok'."""
    return HttpResponse("ok", content_type="text/plain")

urlpatterns = [
    path('', landing_page, name='landing_page'),
    path('login/', stakeholder_login, name='login'),
    path('logout/', stakeholder_logout, name='logout'),
    path('register/', web_register, name='register'),
    path('dashboard/', stakeholder_dashboard, name='stakeholder_dashboard'),
    path('admin/', admin.site.urls),
    path('health/', health_check, name='health_check'),
    path('api/', include('api.urls')),
]

admin.site.site_header = "AfyaGPT Super Admin Dashboard"
admin.site.site_title = "AfyaGPT Health Intelligence Portal"
admin.site.index_title = "Community Health Systems & Triage Control Center"
