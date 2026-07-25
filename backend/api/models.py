from django.db import models
from django.contrib.auth.models import User

class HealthFacility(models.Model):
    """Health Facility / Community Unit model for HMIS organization units."""
    name = models.CharField(max_length=255, unique=True)
    code = models.CharField(max_length=50, blank=True, null=True, unique=True)
    county = models.CharField(max_length=100)
    sub_county = models.CharField(max_length=100, blank=True, null=True)
    ward = models.CharField(max_length=100, blank=True, null=True)
    gps_latitude = models.FloatField(blank=True, null=True)
    gps_longitude = models.FloatField(blank=True, null=True)
    contact_phone = models.CharField(max_length=20, blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['name']

    def __str__(self):
        return f"{self.name} ({self.county})"


class UserProfile(models.Model):
    """Health worker profile model with Role-Based Access Control (RBAC)."""
    ROLE_CHOICES = [
        ('SUPERADMIN', 'Superadmin'),
        ('FACILITY_ADMIN', 'Facility Administrator'),
        ('CLINICIAN', 'Clinician / Nurse'),
        ('CHW', 'Community Health Worker'),
    ]

    full_name = models.CharField(max_length=255)
    phone_number = models.CharField(max_length=20, unique=True)
    email = models.EmailField(blank=True, null=True)
    profession = models.CharField(max_length=100)
    professional_number = models.CharField(max_length=100, blank=True, null=True)
    role = models.CharField(max_length=30, choices=ROLE_CHOICES, default='CHW')
    facility = models.ForeignKey(HealthFacility, on_delete=models.SET_NULL, null=True, blank=True, related_name='staff_members')
    facility_name = models.CharField(max_length=255)
    county = models.CharField(max_length=100)
    sub_county = models.CharField(max_length=100, blank=True, null=True)
    ward = models.CharField(max_length=100, blank=True, null=True)
    malaria_risk_zone = models.CharField(max_length=50, default='HIGH')
    pin_hash = models.CharField(max_length=255, blank=True, null=True)
    is_approved = models.BooleanField(default=False)
    is_active = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    last_active_at = models.DateTimeField(blank=True, null=True)

    def __str__(self):
        return f"{self.full_name} [{self.role}] ({self.facility_name}) - Approved: {self.is_approved}"


class Patient(models.Model):
    """Pediatric patient profile model scoped per facility."""
    patient_uid = models.CharField(max_length=50, unique=True)
    full_name = models.CharField(max_length=255)
    date_of_birth = models.DateField()
    sex = models.CharField(max_length=10, choices=[('Male', 'Male'), ('Female', 'Female')])
    caregiver_name = models.CharField(max_length=255, blank=True, null=True)
    guardian_phone = models.CharField(max_length=20, blank=True, null=True)
    birth_certificate_number = models.CharField(max_length=100, blank=True, null=True)
    facility = models.ForeignKey(HealthFacility, on_delete=models.SET_NULL, null=True, blank=True, related_name='patients')
    facility_name = models.CharField(max_length=255)
    county = models.CharField(max_length=100)
    risk_level = models.CharField(max_length=20, default='LOW')
    created_by = models.ForeignKey(UserProfile, on_delete=models.SET_NULL, null=True, blank=True, related_name='created_patients')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.full_name} ({self.patient_uid}) - {self.facility_name}"


class TriageSession(models.Model):
    """WHO IMCI Triage Assessment session tracking patient visits."""
    VISIT_TYPES = [
        ('FACILITY', 'Facility Visit'),
        ('CHW_HOME_VISIT', 'CHW Home Visit'),
    ]

    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_name='triage_sessions')
    visit_number = models.IntegerField(default=1)
    visit_type = models.CharField(max_length=30, choices=VISIT_TYPES, default='FACILITY')
    visit_location_note = models.CharField(max_length=255, blank=True, null=True)
    gps_latitude = models.FloatField(blank=True, null=True)
    gps_longitude = models.FloatField(blank=True, null=True)
    danger_signs = models.TextField(blank=True, default='')
    temperature = models.FloatField(blank=True, null=True)
    respiratory_rate = models.IntegerField(blank=True, null=True)
    weight_kg = models.FloatField(blank=True, null=True)
    muac_mm = models.FloatField(blank=True, null=True)
    cough_classification = models.CharField(max_length=100, blank=True, null=True)
    diarrhea_classification = models.CharField(max_length=100, blank=True, null=True)
    fever_classification = models.CharField(max_length=100, blank=True, null=True)
    ear_classification = models.CharField(max_length=100, blank=True, null=True)
    nutrition_classification = models.CharField(max_length=100, blank=True, null=True)
    overall_risk = models.CharField(max_length=50, default='LOW')
    treatment_notes = models.TextField(blank=True, default='')
    suggestion_source = models.CharField(max_length=50, default='LOCAL_RULES')
    is_high_risk_flagged = models.BooleanField(default=False)
    created_by = models.ForeignKey(UserProfile, on_delete=models.SET_NULL, null=True, blank=True, related_name='triage_sessions')
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Visit #{self.visit_number} for {self.patient.full_name} [{self.visit_type}] - Risk: {self.overall_risk}"


class Vaccination(models.Model):
    """KEPI Immunization log model."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_name='vaccinations')
    vaccine_name = models.CharField(max_length=100)
    target_disease = models.CharField(max_length=255)
    dose_number = models.IntegerField(default=1)
    is_given = models.BooleanField(default=False)
    given_date = models.DateField(blank=True, null=True)
    batch_number = models.CharField(max_length=100, blank=True, null=True)

    def __str__(self):
        return f"{self.vaccine_name} for {self.patient.full_name} - Given: {self.is_given}"


class ChatMessage(models.Model):
    """AfyaGPT AI Decision Support conversation history model."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_name='chat_messages', blank=True, null=True)
    sender = models.CharField(max_length=20, choices=[('USER', 'USER'), ('AI', 'AI')])
    message_text = models.TextField()
    engine_source = models.CharField(max_length=50, default='LOCAL_RULES')
    timestamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"[{self.sender}] {self.message_text[:30]}..."


class ClinicalFeedback(models.Model):
    """Clinician quality feedback on AI/rules suggestions."""
    triage_session = models.ForeignKey(TriageSession, on_delete=models.CASCADE, related_name='feedbacks')
    clinician = models.ForeignKey(UserProfile, on_delete=models.SET_NULL, null=True, blank=True)
    rating = models.CharField(max_length=20, choices=[('CORRECT', 'Correct'), ('INCORRECT', 'Incorrect'), ('PARTIAL', 'Partially Correct')])
    corrected_classification = models.CharField(max_length=255, blank=True, null=True)
    notes = models.TextField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Feedback on Visit #{self.triage_session.id} by {self.clinician.full_name if self.clinician else 'Anonymous'}: {self.rating}"


class AuditLog(models.Model):
    """System-wide Security & Audit trail for HIPAA/Data Protection compliance."""
    user = models.ForeignKey(UserProfile, on_delete=models.SET_NULL, null=True, blank=True)
    action = models.CharField(max_length=100)
    target_model = models.CharField(max_length=100, blank=True, null=True)
    target_id = models.CharField(max_length=100, blank=True, null=True)
    details = models.TextField(blank=True, null=True)
    timestamp = models.DateTimeField(auto_now_add=True)
    ip_address = models.GenericIPAddressField(blank=True, null=True)

    class Meta:
        ordering = ['-timestamp']

    def __str__(self):
        return f"[{self.timestamp.strftime('%Y-%m-%d %H:%M')}] {self.user.full_name if self.user else 'System'}: {self.action}"


class NewsArticle(models.Model):
    """News and updates model for the landing page."""
    title = models.CharField(max_length=255)
    slug = models.SlugField(unique=True)
    summary = models.TextField()
    content = models.TextField()
    category = models.CharField(max_length=100, default='Health Impact')
    author = models.CharField(max_length=100, default='AfyaGPT Team')
    image_url = models.URLField(blank=True, null=True)
    published_at = models.DateTimeField(auto_now_add=True)
    is_featured = models.BooleanField(default=False)

    class Meta:
        ordering = ['-published_at']

    def __str__(self):
        return self.title


class ContactInquiry(models.Model):
    """Contact form submissions from the landing page."""
    name = models.CharField(max_length=255)
    email = models.EmailField()
    phone = models.CharField(max_length=20, blank=True, null=True)
    subject = models.CharField(max_length=255)
    message = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    is_resolved = models.BooleanField(default=False)

    class Meta:
        ordering = ['-created_at']

    def __str__(self):
        return f"Inquiry from {self.name} - {self.subject}"


class Announcement(models.Model):
    """Ministry of Health & Facility Announcements broadcast to mobile CHWs."""
    title = models.CharField(max_length=255)
    message = models.TextField()
    priority = models.CharField(max_length=20, choices=[('INFO', 'INFO'), ('URGENT', 'URGENT')], default='INFO')
    target_county = models.CharField(max_length=100, default='ALL')
    created_at = models.DateTimeField(auto_now_add=True)
    is_active = models.BooleanField(default=True)

    class Meta:
        ordering = ['-created_at']

    def __str__(self):
        return f"[{self.priority}] {self.title}"


class AppSetting(models.Model):
    """Global system settings managed by superadmins."""
    key = models.CharField(max_length=100, unique=True)
    value = models.TextField()
    description = models.CharField(max_length=255, blank=True, null=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.key} = {self.value}"


class LandingSection(models.Model):
    """
    Editable sections of the public landing page.
    """
    SECTION_TYPES = [
        ('HERO', 'Hero banner'),
        ('ABOUT', 'About'),
        ('FEATURE', 'Feature block'),
        ('STAKEHOLDER', 'Stakeholder or team member'),
        ('CONTACT', 'Contact details'),
        ('FOOTER', 'Footer'),
    ]
    section_type = models.CharField(max_length=20, choices=SECTION_TYPES)
    order = models.PositiveIntegerField(default=0)
    title = models.CharField(max_length=255, blank=True)
    subtitle = models.CharField(max_length=255, blank=True)
    body = models.TextField(blank=True)
    image_url = models.URLField(blank=True, null=True)
    is_active = models.BooleanField(default=True)
    updated_at = models.DateTimeField(auto_now=True)
    updated_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True, blank=True)

    class Meta:
        ordering = ['section_type', 'order']

    def __str__(self):
        return f"{self.get_section_type_display()}: {self.title}"


class Stakeholder(models.Model):
    """
    Team member or stakeholder shown on landing page.
    """
    full_name = models.CharField(max_length=255)
    role = models.CharField(max_length=255)
    bio = models.TextField(blank=True)
    photo_url = models.URLField(blank=True, null=True)
    order = models.PositiveIntegerField(default=0)
    is_active = models.BooleanField(default=True)

    class Meta:
        ordering = ['order']

    def __str__(self):
        return f"{self.full_name} - {self.role}"
