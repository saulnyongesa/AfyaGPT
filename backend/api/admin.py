from django.contrib import admin
from .models import (
    HealthFacility, UserProfile, Patient, TriageSession, Vaccination, ChatMessage,
    ClinicalFeedback, AuditLog, NewsArticle, ContactInquiry, Announcement,
    AppSetting, LandingSection, Stakeholder
)

@admin.register(HealthFacility)
class HealthFacilityAdmin(admin.ModelAdmin):
    list_display = ('name', 'code', 'county', 'sub_county', 'contact_phone', 'created_at')
    search_fields = ('name', 'code', 'county')
    list_filter = ('county',)

@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = ('full_name', 'phone_number', 'role', 'profession', 'facility_name', 'county', 'is_approved', 'is_active', 'created_at')
    search_fields = ('full_name', 'phone_number', 'facility_name', 'county')
    list_filter = ('role', 'profession', 'county', 'is_approved', 'is_active')

@admin.register(Patient)
class PatientAdmin(admin.ModelAdmin):
    list_display = ('patient_uid', 'full_name', 'date_of_birth', 'sex', 'risk_level', 'facility_name', 'created_at')
    search_fields = ('patient_uid', 'full_name', 'caregiver_name', 'guardian_phone')
    list_filter = ('risk_level', 'sex', 'county')

@admin.register(TriageSession)
class TriageSessionAdmin(admin.ModelAdmin):
    list_display = ('patient', 'visit_number', 'visit_type', 'overall_risk', 'suggestion_source', 'is_high_risk_flagged', 'created_at')
    list_filter = ('visit_type', 'overall_risk', 'suggestion_source', 'is_high_risk_flagged')
    search_fields = ('patient__full_name', 'patient__patient_uid')

@admin.register(Vaccination)
class VaccinationAdmin(admin.ModelAdmin):
    list_display = ('patient', 'vaccine_name', 'dose_number', 'is_given', 'given_date')
    list_filter = ('is_given', 'vaccine_name')

@admin.register(ChatMessage)
class ChatMessageAdmin(admin.ModelAdmin):
    list_display = ('patient', 'sender', 'engine_source', 'timestamp')
    list_filter = ('sender', 'engine_source')

@admin.register(ClinicalFeedback)
class ClinicalFeedbackAdmin(admin.ModelAdmin):
    list_display = ('triage_session', 'clinician', 'rating', 'corrected_classification', 'created_at')
    list_filter = ('rating', 'created_at')

@admin.register(AuditLog)
class AuditLogAdmin(admin.ModelAdmin):
    list_display = ('timestamp', 'user', 'action', 'target_model', 'target_id', 'ip_address')
    list_filter = ('action', 'target_model')
    search_fields = ('details', 'user__full_name', 'action')

@admin.register(NewsArticle)
class NewsArticleAdmin(admin.ModelAdmin):
    list_display = ('title', 'category', 'author', 'published_at', 'is_featured')
    prepopulated_fields = {'slug': ('title',)}
    list_filter = ('category', 'is_featured')
    search_fields = ('title', 'summary', 'content')

@admin.register(ContactInquiry)
class ContactInquiryAdmin(admin.ModelAdmin):
    list_display = ('name', 'email', 'phone', 'subject', 'created_at', 'is_resolved')
    list_filter = ('is_resolved', 'created_at')
    search_fields = ('name', 'email', 'subject', 'message')

@admin.register(Announcement)
class AnnouncementAdmin(admin.ModelAdmin):
    list_display = ('title', 'priority', 'target_county', 'created_at', 'is_active')
    list_filter = ('priority', 'is_active', 'target_county')
    search_fields = ('title', 'message')

@admin.register(AppSetting)
class AppSettingAdmin(admin.ModelAdmin):
    list_display = ('key', 'value', 'description', 'updated_at')
    search_fields = ('key', 'description')

@admin.register(LandingSection)
class LandingSectionAdmin(admin.ModelAdmin):
    list_display = ('section_type', 'order', 'title', 'is_active', 'updated_at')
    list_filter = ('section_type', 'is_active')

@admin.register(Stakeholder)
class StakeholderAdmin(admin.ModelAdmin):
    list_display = ('full_name', 'role', 'order', 'is_active')
    list_filter = ('is_active',)
