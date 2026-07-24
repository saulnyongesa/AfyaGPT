from django.contrib import admin
from .models import UserProfile, Patient, TriageSession, Vaccination, ChatMessage

@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = ('full_name', 'phone_number', 'profession', 'facility_name', 'county', 'created_at')
    search_fields = ('full_name', 'phone_number', 'facility_name')

@admin.register(Patient)
class PatientAdmin(admin.ModelAdmin):
    list_display = ('patient_uid', 'full_name', 'date_of_birth', 'sex', 'risk_level', 'facility_name', 'created_at')
    search_fields = ('patient_uid', 'full_name', 'caregiver_name', 'guardian_phone')
    list_filter = ('risk_level', 'sex', 'county')

@admin.register(TriageSession)
class TriageSessionAdmin(admin.ModelAdmin):
    list_display = ('patient', 'overall_risk', 'cough_classification', 'diarrhea_classification', 'fever_classification', 'created_at')
    list_filter = ('overall_risk',)

@admin.register(Vaccination)
class VaccinationAdmin(admin.ModelAdmin):
    list_display = ('patient', 'vaccine_name', 'dose_number', 'is_given', 'given_date')
    list_filter = ('is_given', 'vaccine_name')

@admin.register(ChatMessage)
class ChatMessageAdmin(admin.ModelAdmin):
    list_display = ('patient', 'sender', 'timestamp')
    list_filter = ('sender',)
