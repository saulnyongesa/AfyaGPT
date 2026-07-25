import csv
import json
from datetime import timedelta
from django.utils import timezone
from django.http import HttpResponse, JsonResponse
from django.shortcuts import render, redirect, get_object_or_404
from django.contrib import messages
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from rest_framework import viewsets, status, permissions
from rest_framework.views import APIView
from rest_framework.response import Response

from .models import (
    HealthFacility, UserProfile, Patient, TriageSession, Vaccination, ChatMessage,
    ClinicalFeedback, AuditLog, NewsArticle, ContactInquiry, Announcement,
    AppSetting, LandingSection, Stakeholder
)
from .serializers import (
    HealthFacilitySerializer, UserProfileSerializer, PatientSerializer,
    TriageSessionSerializer, VaccinationSerializer, ChatMessageSerializer,
    ClinicalFeedbackSerializer, AuditLogSerializer, NewsArticleSerializer,
    ContactInquirySerializer, AnnouncementSerializer, AppSettingSerializer,
    LandingSectionSerializer, StakeholderSerializer
)


def log_audit_event(user_profile, action, target_model="", target_id="", details="", request=None):
    """Helper to record audit trail entries for HIPAA & HMIS data compliance."""
    ip_addr = None
    if request:
        x_forwarded_for = request.META.get('HTTP_X_FORWARDED_FOR')
        if x_forwarded_for:
            ip_addr = x_forwarded_for.split(',')[0]
        else:
            ip_addr = request.META.get('REMOTE_ADDR')

    AuditLog.objects.create(
        user=user_profile,
        action=action,
        target_model=target_model,
        target_id=str(target_id),
        details=details,
        ip_address=ip_addr
    )


def landing_page(request):
    """Renders the official AfyaGPT landing page with dynamic stats, news, and contact form."""
    if request.method == 'POST':
        name = request.POST.get('name', '').strip()
        email = request.POST.get('email', '').strip()
        phone = request.POST.get('phone', '').strip()
        subject = request.POST.get('subject', '').strip()
        message = request.POST.get('message', '').strip()

        if name and email and message:
            ContactInquiry.objects.create(
                name=name, email=email, phone=phone, subject=subject or 'General Inquiry', message=message
            )
            messages.success(request, 'Thank you! Your message has been received. Our team will contact you shortly.')
            return redirect('/#contact')
        else:
            messages.error(request, 'Please complete all required fields.')

    stats = {
        'total_patients': Patient.objects.count(),
        'total_triage': TriageSession.objects.count(),
        'total_vaccines': Vaccination.objects.filter(is_given=True).count(),
        'total_workers': UserProfile.objects.filter(is_approved=True).count(),
    }

    sections = LandingSection.objects.filter(is_active=True)
    stakeholders = Stakeholder.objects.filter(is_active=True)
    news_articles = NewsArticle.objects.all()[:3]

    facilities_qs = HealthFacility.objects.filter(is_draft=False)
    facilities_data = []
    for f in facilities_qs:
        facilities_data.append({
            'id': f.id,
            'name': f.name,
            'facility_type': f.facility_type,
            'county': f.county or 'N/A',
            'sub_county': f.sub_county or '',
            'village': f.village or '',
            'phone': f.contact_phone or '',
            'lat': float(f.gps_latitude) if f.gps_latitude else -1.286389,
            'lng': float(f.gps_longitude) if f.gps_longitude else 36.817223
        })

    return render(request, 'index.html', {
        'stats': stats,
        'sections': sections,
        'stakeholders': stakeholders,
        'news_articles': news_articles,
        'facilities': facilities_qs,
        'facilities_json': json.dumps(facilities_data)
    })


def dashboard_live_stream(request):
    """Real-time light delta streaming endpoint for dashboard updates without full page reloads."""
    since_timestamp = request.GET.get('since')
    facility_id = request.session.get('facility_id')
    
    facility = HealthFacility.objects.filter(id=facility_id).first() if facility_id else None
    
    query_patients = Patient.objects.all()
    query_triage = TriageSession.objects.all()
    query_users = UserProfile.objects.all()

    if facility:
        query_patients = query_patients.filter(facility_name=facility.name)
        query_triage = query_triage.filter(patient__facility_name=facility.name)
        query_users = query_users.filter(facility=facility)

    if since_timestamp:
        try:
            from django.utils.dateparse import parse_datetime
            dt = parse_datetime(since_timestamp)
            if dt:
                query_patients = query_patients.filter(created_at__gt=dt)
                query_triage = query_triage.filter(created_at__gt=dt)
                query_users = query_users.filter(created_at__gt=dt)
        except Exception:
            pass

    new_patients = [
        {
            'id': p.id,
            'uid': p.patient_uid,
            'name': p.full_name,
            'dob': p.date_of_birth.strftime('%Y-%m-%d') if p.date_of_birth else '',
            'risk': p.risk_level,
            'created_at': p.created_at.strftime('%Y-%m-%d %H:%M')
        }
        for p in query_patients.order_by('-created_at')[:10]
    ]

    new_triage = [
        {
            'id': t.id,
            'session_uid': t.session_uid,
            'patient_name': t.patient.full_name if t.patient else 'Unknown',
            'overall_risk': t.overall_risk,
            'created_at': t.created_at.strftime('%Y-%m-%d %H:%M')
        }
        for t in query_triage.order_by('-created_at')[:10]
    ]

    new_pending_users = [
        {
            'id': u.id,
            'name': u.full_name,
            'profession': u.profession,
            'created_at': u.created_at.strftime('%Y-%m-%d %H:%M')
        }
        for u in query_users.filter(is_approved=False).order_by('-created_at')[:5]
    ]

    stats = {
        'total_patients': Patient.objects.filter(facility_name=facility.name).count() if facility else Patient.objects.count(),
        'total_triage': TriageSession.objects.filter(patient__facility_name=facility.name).count() if facility else TriageSession.objects.count(),
        'pending_approvals': UserProfile.objects.filter(facility=facility, is_approved=False).count() if facility else UserProfile.objects.filter(is_approved=False).count()
    }

    return JsonResponse({
        'server_timestamp': timezone.now().isoformat(),
        'stats': stats,
        'new_patients': new_patients,
        'new_triage': new_triage,
        'new_pending_users': new_pending_users
    })


def web_register(request):
    """Registration view for Health Facilities and their Facility Admin account."""
    if request.method == 'POST':
        name = request.POST.get('name', '').strip()
        email = request.POST.get('email', '').strip()
        country = request.POST.get('country', 'Kenya').strip()
        county = request.POST.get('county', '').strip()
        sub_county = request.POST.get('sub_county', '').strip()
        village = request.POST.get('village', '').strip()
        address = request.POST.get('address', '').strip()
        contact_phone = request.POST.get('contact_phone', '').strip()
        website = request.POST.get('website', '').strip()
        facebook_url = request.POST.get('facebook_url', '').strip()
        twitter_url = request.POST.get('twitter_url', '').strip()
        gps_lat = request.POST.get('gps_latitude')
        gps_lng = request.POST.get('gps_longitude')
        password = request.POST.get('password', '').strip()
        confirm_password = request.POST.get('confirm_password', '').strip()

        if not name or not email:
            messages.error(request, 'Facility Name and Official Email are required.')
            return render(request, 'register.html')

        if password and confirm_password and password != confirm_password:
            messages.error(request, 'Passwords do not match. Please try again.')
            return render(request, 'register.html')

        # Mandatory core required fields for account activation (is_draft = False)
        # Website and social URLs are optional.
        has_core_info = bool(name and email and county and contact_phone and gps_lat and gps_lng and password)
        is_draft = not has_core_info

        # Create or update HealthFacility record
        facility, created = HealthFacility.objects.update_or_create(
            email=email,
            defaults={
                'name': name,
                'country': country,
                'county': county or 'Nairobi',
                'sub_county': sub_county or None,
                'village': village or None,
                'address': address or None,
                'contact_phone': contact_phone or None,
                'website': website or None,
                'facebook_url': facebook_url or None,
                'twitter_url': twitter_url or None,
                'gps_latitude': gps_lat or None,
                'gps_longitude': gps_lng or None,
                'is_draft': is_draft
            }
        )

        # Create Django User & UserProfile for Facility Admin if password provided
        if password:
            user_obj, _ = User.objects.get_or_create(username=email, defaults={'email': email, 'is_staff': True})
            user_obj.set_password(password)
            user_obj.save()

            UserProfile.objects.update_or_create(
                email=email,
                defaults={
                    'full_name': f"{name} Admin",
                    'phone_number': contact_phone or f"07{facility.id:08d}",
                    'profession': 'FACILITY_ADMIN',
                    'role': 'FACILITY_ADMIN',
                    'facility': facility,
                    'facility_name': name,
                    'county': county or 'Nairobi',
                    'is_approved': True,
                    'is_active': True
                }
            )

        log_audit_event(None, 'FACILITY_WEB_REGISTERED', 'HealthFacility', facility.id, f"Registered facility {name} (is_draft={is_draft})", request)

        if is_draft:
            messages.warning(request, f"Facility '{name}' saved as draft. Please complete location permission and all required fields to activate account.")
            return redirect('/register/')
        else:
            messages.success(request, f"Health Facility '{name}' registered and activated successfully! You can now log in with your email and password.")
            return redirect('/login/')

    return render(request, 'register.html')


def stakeholder_login(request):
    """Authentication view for Admins and Staff."""
    if request.user.is_authenticated:
        return redirect('/dashboard/')

    if request.method == 'POST':
        username = request.POST.get('username', '').strip()
        password = request.POST.get('password', '').strip()
        user = authenticate(request, username=username, password=password)

        if user is not None:
            login(request, user)
            messages.success(request, f"Welcome back, {user.username}!")
            return redirect('/dashboard/')
        else:
            messages.error(request, "Invalid username or password. Please try again.")

    return render(request, 'login.html')


def stakeholder_logout(request):
    """Logout view for Stakeholder portal."""
    logout(request)
    messages.info(request, "You have been logged out successfully.")
    return redirect('/login/')


@login_required(login_url='/login/')
def stakeholder_dashboard(request):
    """
    Comprehensive 10-Category Dashboard aligned with DHIS2 HMIS standards,
    RBAC user management, facility operations, clinical quality feedback,
    and superadmin CMS editing.
    """
    user = request.user
    user_groups = list(user.groups.values_list('name', flat=True))

    # Timeframe filter query parameter
    time_filter = request.GET.get('timeframe', 'month')
    now = timezone.now()
    if time_filter == 'today':
        start_date = now.replace(hour=0, minute=0, second=0, microsecond=0)
    elif time_filter == 'week':
        start_date = now - timedelta(days=7)
    elif time_filter == 'month':
        start_date = now - timedelta(days=30)
    else:
        start_date = None

    if request.method == 'POST':
        action = request.POST.get('action')
        admin_profile = UserProfile.objects.filter(email=user.email).first()

        # 1. User Management Actions
        if action == 'approve_user' and user.is_superuser:
            user_id = request.POST.get('user_id')
            profile = UserProfile.objects.filter(id=user_id).first()
            if profile:
                profile.is_approved = not profile.is_approved
                profile.save()
                log_audit_event(admin_profile, 'USER_APPROVAL_TOGGLED', 'UserProfile', profile.id, f"Approved status set to {profile.is_approved}", request)
                messages.success(request, f"Health Worker {profile.full_name} status set to {'Approved' if profile.is_approved else 'Pending'}.")

        elif action == 'toggle_active_user' and user.is_superuser:
            user_id = request.POST.get('user_id')
            profile = UserProfile.objects.filter(id=user_id).first()
            if profile:
                profile.is_active = not profile.is_active
                profile.save()
                log_audit_event(admin_profile, 'USER_ACTIVE_TOGGLED', 'UserProfile', profile.id, f"Is_active set to {profile.is_active}", request)
                messages.success(request, f"Account for {profile.full_name} is now {'Active' if profile.is_active else 'Deactivated'}.")

        elif action == 'change_user_role' and user.is_superuser:
            user_id = request.POST.get('user_id')
            new_role = request.POST.get('role', 'CHW')
            profile = UserProfile.objects.filter(id=user_id).first()
            if profile:
                profile.role = new_role
                profile.save()
                log_audit_event(admin_profile, 'USER_ROLE_CHANGED', 'UserProfile', profile.id, f"Role changed to {new_role}", request)
                messages.success(request, f"Role for {profile.full_name} updated to {new_role}.")

        elif action == 'reset_user_pin' and user.is_superuser:
            user_id = request.POST.get('user_id')
            new_pin = request.POST.get('new_pin', '123456').strip()
            profile = UserProfile.objects.filter(id=user_id).first()
            if profile:
                profile.pin_hash = new_pin
                profile.save()
                log_audit_event(admin_profile, 'USER_PIN_RESET', 'UserProfile', profile.id, 'Admin triggered PIN reset', request)
                messages.success(request, f"PIN for {profile.full_name} successfully reset to {new_pin}.")

        # 2. Facility Actions
        elif action == 'create_facility':
            f_name = request.POST.get('name', '').strip()
            f_code = request.POST.get('code', '').strip()
            f_type = request.POST.get('facility_type', 'DISPENSARY').strip()
            f_county = request.POST.get('county', 'Nairobi').strip()
            f_subcounty = request.POST.get('sub_county', '').strip()
            f_village = request.POST.get('village', '').strip()
            f_phone = request.POST.get('contact_phone', '').strip()
            f_email = request.POST.get('email', '').strip()
            f_lat = request.POST.get('gps_latitude')
            f_lng = request.POST.get('gps_longitude')

            if f_name:
                facility = HealthFacility.objects.create(
                    name=f_name,
                    code=f_code or None,
                    facility_type=f_type,
                    county=f_county,
                    sub_county=f_subcounty or None,
                    village=f_village or None,
                    contact_phone=f_phone or None,
                    email=f_email or None,
                    gps_latitude=f_lat or None,
                    gps_longitude=f_lng or None,
                    is_draft=False
                )
                log_audit_event(admin_profile, 'FACILITY_CREATED', 'HealthFacility', facility.id, f"Created {f_name}", request)
                messages.success(request, f"Health Facility '{f_name}' registered successfully!")
            else:
                messages.error(request, "Facility name is required.")

        # 3. Communications Actions
        elif action == 'create_announcement':
            title = request.POST.get('title')
            message_text = request.POST.get('message')
            priority = request.POST.get('priority', 'INFO')
            if title and message_text:
                ann = Announcement.objects.create(title=title, message=message_text, priority=priority)
                log_audit_event(admin_profile, 'ANNOUNCEMENT_CREATED', 'Announcement', ann.id, title, request)
                messages.success(request, "Announcement broadcasted to mobile health workers.")

        # 4. Clinical Quality Feedback
        elif action == 'submit_feedback':
            session_id = request.POST.get('session_id')
            rating = request.POST.get('rating', 'CORRECT')
            corrected = request.POST.get('corrected_classification', '')
            notes = request.POST.get('notes', '')
            t_session = TriageSession.objects.filter(id=session_id).first()
            if t_session:
                fb = ClinicalFeedback.objects.create(
                    triage_session=t_session, clinician=admin_profile,
                    rating=rating, corrected_classification=corrected, notes=notes
                )
                log_audit_event(admin_profile, 'CLINICAL_FEEDBACK_SUBMITTED', 'ClinicalFeedback', fb.id, f"Rating: {rating}", request)
                messages.success(request, "Clinical suggestion feedback recorded.")

        # 5. AI Engine Setting Toggle
        elif action == 'toggle_ai_engine' and user.is_superuser:
            setting, _ = AppSetting.objects.get_or_create(key='AI_ENGINE_ENABLED', defaults={'value': 'true', 'description': 'Enable/Disable remote AI engine'})
            new_val = 'false' if setting.value.lower() == 'true' else 'true'
            setting.value = new_val
            setting.save()
            log_audit_event(admin_profile, 'AI_ENGINE_TOGGLED', 'AppSetting', setting.id, f"AI Engine set to {new_val}", request)
            messages.success(request, f"AI Decision Support engine set to {'ENABLED' if new_val == 'true' else 'DISABLED (Local Rules Only)'}.")

        # 6. CMS Editor Actions
        elif action == 'update_section' and user.is_superuser:
            section_id = request.POST.get('section_id')
            section = LandingSection.objects.filter(id=section_id).first()
            if section:
                section.title = request.POST.get('title', section.title)
                section.subtitle = request.POST.get('subtitle', section.subtitle)
                section.body = request.POST.get('body', section.body)
                section.image_url = request.POST.get('image_url', section.image_url)
                section.updated_by = user
                section.save()
                log_audit_event(admin_profile, 'CMS_SECTION_UPDATED', 'LandingSection', section.id, section.title, request)
                messages.success(request, f"Updated {section.get_section_type_display()} section.")

        elif action == 'save_stakeholder' and user.is_superuser:
            stakeholder_id = request.POST.get('stakeholder_id')
            stakeholder = Stakeholder.objects.filter(id=stakeholder_id).first() if stakeholder_id else Stakeholder()
            stakeholder.full_name = request.POST.get('full_name', '')
            stakeholder.role = request.POST.get('role', '')
            stakeholder.bio = request.POST.get('bio', '')
            stakeholder.photo_url = request.POST.get('photo_url', '')
            stakeholder.save()
            log_audit_event(admin_profile, 'CMS_STAKEHOLDER_SAVED', 'Stakeholder', stakeholder.id, stakeholder.full_name, request)
            messages.success(request, f"Saved stakeholder {stakeholder.full_name}.")

        # 7. Comprehensive Deletion Actions
        elif action == 'delete_user' and user.is_superuser:
            user_id = request.POST.get('target_id')
            u_obj = UserProfile.objects.filter(id=user_id).first()
            if u_obj:
                name = u_obj.full_name
                u_obj.delete()
                log_audit_event(admin_profile, 'USER_DELETED', 'UserProfile', user_id, f"Deleted staff {name}", request)
                messages.success(request, f"Staff account for {name} has been permanently deleted.")

        elif action == 'delete_patient' and user.is_superuser:
            p_id = request.POST.get('target_id')
            p_obj = Patient.objects.filter(id=p_id).first()
            if p_obj:
                name = p_obj.full_name
                p_obj.delete()
                log_audit_event(admin_profile, 'PATIENT_DELETED', 'Patient', p_id, f"Deleted patient {name}", request)
                messages.success(request, f"Patient profile for {name} has been deleted.")

        elif action == 'edit_patient' and user.is_superuser:
            p_id = request.POST.get('patient_id')
            p_obj = Patient.objects.filter(id=p_id).first()
            if p_obj:
                p_obj.full_name = request.POST.get('full_name', p_obj.full_name).strip()
                p_obj.caregiver_name = request.POST.get('caregiver_name', p_obj.caregiver_name).strip()
                p_obj.guardian_phone = request.POST.get('guardian_phone', p_obj.guardian_phone).strip()
                p_obj.facility_name = request.POST.get('facility_name', p_obj.facility_name).strip()
                p_obj.risk_level = request.POST.get('risk_level', p_obj.risk_level).strip()
                p_obj.save()
                log_audit_event(admin_profile, 'PATIENT_EDITED', 'Patient', p_id, f"Updated patient {p_obj.full_name}", request)
                messages.success(request, f"Updated patient record for {p_obj.full_name}.")

        elif action == 'delete_triage' and user.is_superuser:
            t_id = request.POST.get('target_id')
            t_obj = TriageSession.objects.filter(id=t_id).first()
            if t_obj:
                t_obj.delete()
                log_audit_event(admin_profile, 'TRIAGE_DELETED', 'TriageSession', t_id, "Deleted triage session", request)
                messages.success(request, f"Triage assessment record #{t_id} deleted.")

        elif action == 'delete_facility' and user.is_superuser:
            f_id = request.POST.get('target_id')
            f_obj = HealthFacility.objects.filter(id=f_id).first()
            if f_obj:
                name = f_obj.name
                f_obj.delete()
                log_audit_event(admin_profile, 'FACILITY_DELETED', 'HealthFacility', f_id, f"Deleted facility {name}", request)
                messages.success(request, f"Health facility {name} removed.")

        elif action == 'delete_stakeholder' and user.is_superuser:
            s_id = request.POST.get('target_id')
            s_obj = Stakeholder.objects.filter(id=s_id).first()
            if s_obj:
                name = s_obj.full_name
                s_obj.delete()
                log_audit_event(admin_profile, 'STAKEHOLDER_DELETED', 'Stakeholder', s_id, f"Deleted stakeholder {name}", request)
                messages.success(request, f"Stakeholder {name} removed from CMS.")

        elif action == 'delete_announcement':
            a_id = request.POST.get('target_id')
            a_obj = Announcement.objects.filter(id=a_id).first()
            if a_obj:
                a_obj.delete()
                log_audit_event(admin_profile, 'ANNOUNCEMENT_DELETED', 'Announcement', a_id, "Deleted announcement", request)
                messages.success(request, "Broadcast announcement deleted.")

        return redirect('/dashboard/')

    # Query Datasets with Time Filters
    patient_qs = Patient.objects.all()
    triage_qs = TriageSession.objects.all()
    if start_date:
        patient_qs = patient_qs.filter(created_at__gte=start_date)
        triage_qs = triage_qs.filter(created_at__gte=start_date)

    # Aggregates for Dashboard Counters
    total_patients_count = patient_qs.count()
    total_triage_count = triage_qs.count()

    risk_green = triage_qs.filter(overall_risk__iexact='LOW').count()
    risk_yellow = triage_qs.filter(overall_risk__iexact='MEDIUM').count()
    risk_red = triage_qs.filter(overall_risk__in=['HIGH', 'CRITICAL']).count()

    # Home visits vs Facility visits
    facility_visits_count = triage_qs.filter(visit_type='FACILITY').count()
    home_visits_count = triage_qs.filter(visit_type='CHW_HOME_VISIT').count()

    # Suggestion Engine Distribution
    rules_engine_count = triage_qs.filter(suggestion_source='LOCAL_RULES').count()
    ai_engine_count = triage_qs.filter(suggestion_source='REMOTE_AI').count()

    # Immunization Summary
    total_vaccines_given = Vaccination.objects.filter(is_given=True).count()
    total_vaccines_pending = Vaccination.objects.filter(is_given=False).count()
    immunization_rate = round((total_vaccines_given / max(1, (total_vaccines_given + total_vaccines_pending))) * 100, 1)

    # GPS Home Visit Map Data
    gps_markers = []
    for t in triage_qs.filter(gps_latitude__isnull=False, gps_longitude__isnull=False)[:50]:
        gps_markers.append({
            'lat': t.gps_latitude,
            'lng': t.gps_longitude,
            'patient_name': t.patient.full_name,
            'risk': t.overall_risk,
            'visit_type': t.get_visit_type_display(),
            'date': t.created_at.strftime('%b %d, %Y')
        })

    # System AI Engine setting state
    ai_setting = AppSetting.objects.filter(key='AI_ENGINE_ENABLED').first()
    ai_enabled = ai_setting.value.lower() == 'true' if ai_setting else True

    context = {
        'is_superuser': user.is_superuser,
        'user_groups': user_groups,
        'timeframe': time_filter,
        'total_patients': total_patients_count,
        'total_triage': total_triage_count,
        'risk_green': risk_green,
        'risk_yellow': risk_yellow,
        'risk_red': risk_red,
        'facility_visits_count': facility_visits_count,
        'home_visits_count': home_visits_count,
        'rules_engine_count': rules_engine_count,
        'ai_engine_count': ai_engine_count,
        'total_vaccines_given': total_vaccines_given,
        'immunization_rate': immunization_rate,
        'ai_enabled': ai_enabled,
        'gps_markers_json': json.dumps(gps_markers),

        'patients': patient_qs.order_by('-created_at')[:25],
        'triages': triage_qs.order_by('-created_at')[:25],
        'high_risk_patients': Patient.objects.filter(risk_level__in=['HIGH', 'CRITICAL']).order_by('-updated_at')[:15],
        'users': UserProfile.objects.all().order_by('-created_at'),
        'pending_users': UserProfile.objects.filter(is_approved=False).count(),
        'facilities': HealthFacility.objects.all(),
        'announcements': Announcement.objects.filter(is_active=True)[:10],
        'landing_sections': LandingSection.objects.all(),
        'stakeholders': Stakeholder.objects.all(),
        'audit_logs': AuditLog.objects.all()[:30],
        'feedbacks': ClinicalFeedback.objects.all().order_by('-created_at')[:20],
        'settings': AppSetting.objects.all(),
    }
    return render(request, 'dashboard.html', context)


# ─── Exportable Reports Endpoints ─────────────────────────────────────────────

@login_required(login_url='/login/')
def export_patients_csv(request):
    """Exports patient registry as CSV report."""
    response = HttpResponse(content_type='text/csv')
    response['Content-Disposition'] = 'attachment; filename="AfyaGPT_Patient_Registry.csv"'

    writer = csv.writer(response)
    writer.writerow(['Patient UID', 'Full Name', 'DOB', 'Sex', 'Caregiver', 'Guardian Phone', 'Facility', 'County', 'Risk Level', 'Registered Date'])

    for p in Patient.objects.all().order_by('-created_at'):
        writer.writerow([
            p.patient_uid, p.full_name, p.date_of_birth, p.sex,
            p.caregiver_name or '', p.guardian_phone or '',
            p.facility_name, p.county, p.risk_level,
            p.created_at.strftime('%Y-%m-%d %H:%M')
        ])
    return response


@login_required(login_url='/login/')
def export_triage_csv(request):
    """Exports triage assessment history as CSV report."""
    response = HttpResponse(content_type='text/csv')
    response['Content-Disposition'] = 'attachment; filename="AfyaGPT_Triage_Assessments.csv"'

    writer = csv.writer(response)
    writer.writerow([
        'Visit ID', 'Patient UID', 'Patient Name', 'Visit Type', 'Overall Risk',
        'Cough Class', 'Diarrhea Class', 'Fever Class', 'Ear Class', 'Nutrition Class',
        'Suggestion Source', 'Assessed Date'
    ])

    for t in TriageSession.objects.all().order_by('-created_at'):
        writer.writerow([
            t.id, t.patient.patient_uid, t.patient.full_name, t.get_visit_type_display(),
            t.overall_risk, t.cough_classification or '', t.diarrhea_classification or '',
            t.fever_classification or '', t.ear_classification or '', t.nutrition_classification or '',
            t.suggestion_source, t.created_at.strftime('%Y-%m-%d %H:%M')
        ])
    return response


@login_required(login_url='/login/')
def export_hmis_indicators_csv(request):
    """Exports Kenya HMIS / DHIS2 Aligned Summary Indicators as CSV."""
    response = HttpResponse(content_type='text/csv')
    response['Content-Disposition'] = 'attachment; filename="AfyaGPT_HMIS_DHIS2_Indicators.csv"'

    writer = csv.writer(response)
    writer.writerow(['HMIS Indicator Code', 'Indicator Description', 'Value', 'Reporting Unit'])

    total_p = max(1, Patient.objects.count())
    total_t = max(1, TriageSession.objects.count())
    high_risk_count = TriageSession.objects.filter(overall_risk__in=['HIGH', 'CRITICAL']).count()
    vax_given = Vaccination.objects.filter(is_given=True).count()
    vax_total = max(1, Vaccination.objects.count())

    writer.writerow(['MOH-711-01', 'Total Children Under-5 Registered', Patient.objects.count(), 'Children'])
    writer.writerow(['MOH-711-02', 'Total Triage Assessments Completed', TriageSession.objects.count(), 'Visits'])
    writer.writerow(['MOH-711-03', 'High Risk / Severe Cases Identified', high_risk_count, 'Cases'])
    writer.writerow(['MOH-711-04', 'Proportion High Risk Referred (%)', round((high_risk_count / total_t) * 100, 1), '%'])
    writer.writerow(['MOH-710-01', 'KEPI Immunization Coverage Rate (%)', round((vax_given / vax_total) * 100, 1), '%'])
    writer.writerow(['MOH-711-05', 'CHW Home Visit Assessments Ratio (%)', round((TriageSession.objects.filter(visit_type='CHW_HOME_VISIT').count() / total_t) * 100, 1), '%'])

    return response


# ─── REST ViewSets ─────────────────────────────────────────────────────────────

class HealthFacilityViewSet(viewsets.ModelViewSet):
    queryset = HealthFacility.objects.filter(is_draft=False)
    serializer_class = HealthFacilitySerializer
    permission_classes = [permissions.AllowAny]


class WhoRulesView(APIView):
    """WHO IMCI Rules API endpoint for mobile client online rule sync."""
    permission_classes = [permissions.AllowAny]

    def get(self, request):
        rules = [
            {
                "rule_id": "IMCI-RESP-01",
                "category": "RESPIRATORY",
                "condition": "Cough or Difficult Breathing",
                "min_age_months": 2,
                "max_age_months": 59,
                "fast_breathing_cutoff": 50,
                "danger_signs": ["Chest indrawing", "Stridor in calm child", "Unable to drink", "Convulsions"],
                "version": "2026.1"
            },
            {
                "rule_id": "IMCI-DIARRHEA-01",
                "category": "DIARRHEA",
                "condition": "Diarrhea Assessment",
                "min_age_months": 2,
                "max_age_months": 59,
                "dehydration_signs": ["Sunken eyes", "Skin pinch goes back very slowly (>2s)", "Lethargic"],
                "version": "2026.1"
            },
            {
                "rule_id": "IMCI-FEVER-01",
                "category": "FEVER",
                "condition": "Fever Assessment (Malaria Risk)",
                "min_age_months": 2,
                "max_age_months": 59,
                "malaria_risk_zones": ["HIGH", "MEDIUM", "LOW"],
                "version": "2026.1"
            }
        ]
        return Response({'version': '2026.1', 'rules': rules}, status=status.HTTP_200_OK)

class UserProfileViewSet(viewsets.ModelViewSet):
    queryset = UserProfile.objects.all().order_by('-created_at')
    serializer_class = UserProfileSerializer

class PatientViewSet(viewsets.ModelViewSet):
    queryset = Patient.objects.all().order_by('-created_at')
    serializer_class = PatientSerializer

class TriageSessionViewSet(viewsets.ModelViewSet):
    queryset = TriageSession.objects.all().order_by('-created_at')
    serializer_class = TriageSessionSerializer

class VaccinationViewSet(viewsets.ModelViewSet):
    queryset = Vaccination.objects.all()
    serializer_class = VaccinationSerializer

class ChatMessageViewSet(viewsets.ModelViewSet):
    queryset = ChatMessage.objects.all().order_by('timestamp')
    serializer_class = ChatMessageSerializer

class ClinicalFeedbackViewSet(viewsets.ModelViewSet):
    queryset = ClinicalFeedback.objects.all().order_by('-created_at')
    serializer_class = ClinicalFeedbackSerializer

class AuditLogViewSet(viewsets.ModelViewSet):
    queryset = AuditLog.objects.all().order_by('-timestamp')
    serializer_class = AuditLogSerializer

class NewsArticleViewSet(viewsets.ModelViewSet):
    queryset = NewsArticle.objects.all()
    serializer_class = NewsArticleSerializer

class ContactInquiryViewSet(viewsets.ModelViewSet):
    queryset = ContactInquiry.objects.all().order_by('-created_at')
    serializer_class = ContactInquirySerializer

class AnnouncementViewSet(viewsets.ModelViewSet):
    queryset = Announcement.objects.filter(is_active=True).order_by('-created_at')
    serializer_class = AnnouncementSerializer

class AppSettingViewSet(viewsets.ModelViewSet):
    queryset = AppSetting.objects.all()
    serializer_class = AppSettingSerializer

class LandingSectionViewSet(viewsets.ModelViewSet):
    queryset = LandingSection.objects.filter(is_active=True).order_by('order')
    serializer_class = LandingSectionSerializer

class StakeholderViewSet(viewsets.ModelViewSet):
    queryset = Stakeholder.objects.filter(is_active=True).order_by('order')
    serializer_class = StakeholderSerializer


class MobileLoginView(APIView):
    """Mobile Login Authentication View (/api/auth/login/)."""
    def post(self, request):
        identifier = request.data.get('identifier', '').strip()
        user = UserProfile.objects.filter(phone_number=identifier).first() or UserProfile.objects.filter(email=identifier).first()
        if user:
            if not user.is_approved:
                return Response({'error': 'Account pending admin approval. Please contact your supervisor.'}, status=status.HTTP_403_FORBIDDEN)
            if not user.is_active:
                return Response({'error': 'Account deactivated. Please contact your administrator.'}, status=status.HTTP_403_FORBIDDEN)
            
            user.last_active_at = timezone.now()
            user.save()
            serializer = UserProfileSerializer(user)
            return Response({'status': 'authenticated', 'user': serializer.data}, status=status.HTTP_200_OK)
        return Response({'error': f"User account with identifier '{identifier}' not found on cloud server."}, status=status.HTTP_404_NOT_FOUND)


class MobileRegisterView(APIView):
    """Mobile App Registration View (/api/auth/register/)."""
    def post(self, request):
        phone = request.data.get('phoneNumber') or request.data.get('phone', '')
        email = request.data.get('email', '')
        full_name = request.data.get('fullName') or request.data.get('full_name', '')
        profession = request.data.get('profession', 'COMMUNITY_HEALTH_WORKER')
        prof_num = request.data.get('professionalNumber') or request.data.get('professional_number', '')
        facility_name = request.data.get('facilityName') or request.data.get('facility_name', 'Health Center')
        county = request.data.get('county', 'Nairobi')
        pin_hash = request.data.get('pinHash', '')

        if phone and UserProfile.objects.filter(phone_number=phone).exists():
            return Response({'error': 'An account with this phone number already exists on the server.'}, status=status.HTTP_400_BAD_REQUEST)
        
        user_prof = UserProfile.objects.create(
            full_name=full_name,
            phone_number=phone,
            email=email or None,
            profession=profession,
            role='CHW' if 'CHW' in profession or 'COMMUNITY' in profession else 'CLINICIAN',
            professional_number=prof_num or None,
            facility_name=facility_name,
            county=county,
            pin_hash=pin_hash,
            is_approved=True,
            is_active=True
        )
        serializer = UserProfileSerializer(user_prof)
        return Response({'status': 'registered', 'user': serializer.data}, status=status.HTTP_201_CREATED)


class MobileAuthCheckView(APIView):
    """Mobile Login Authentication View compatibility route."""
    def post(self, request):
        return MobileLoginView().post(request)


class BatchSyncView(APIView):
    """Bi-directional Delta Sync View.
    Uploads local offline patient visits and downloads recent facility patient records.
    """
    def post(self, request):
        patients_data = request.data.get('patients', [])
        facility_name = request.data.get('facilityName') or request.data.get('facility_name', '')
        synced_count = 0
        
        # 1. Silent Upsert (No Duplicate Patients)
        for pdata in patients_data:
            try:
                p_uid = pdata.get('patientUid') or pdata.get('patient_uid')
                if not p_uid:
                    continue

                dob_val = pdata.get('dateOfBirth') or pdata.get('date_of_birth') or '2024-01-01'
                if len(dob_val) < 10:
                    dob_val = '2024-01-01'

                patient, created = Patient.objects.update_or_create(
                    patient_uid=p_uid,
                    defaults={
                        'full_name': pdata.get('fullName') or pdata.get('full_name', 'Patient'),
                        'date_of_birth': dob_val,
                        'sex': pdata.get('sex', 'Male'),
                        'caregiver_name': pdata.get('caregiverName') or pdata.get('caregiver_name'),
                        'guardian_phone': pdata.get('guardianPhone') or pdata.get('guardian_phone'),
                        'birth_certificate_number': pdata.get('birthCertificateNumber') or pdata.get('birth_certificate_number'),
                        'facility_name': pdata.get('facilityName') or pdata.get('facility_name') or facility_name,
                        'county': pdata.get('county', 'Nairobi'),
                        'risk_level': pdata.get('riskLevel') or pdata.get('risk_level', 'LOW')
                    }
                )
                
                # Record Triage Visit
                danger_signs = pdata.get('dangerSigns', '')
                if danger_signs or pdata.get('overallRisk'):
                    visit_count = patient.triage_sessions.count() + 1
                    TriageSession.objects.create(
                        patient=patient,
                        visit_number=visit_count,
                        visit_type=pdata.get('visitType', 'FACILITY'),
                        visit_location_note=pdata.get('visitLocationNote'),
                        gps_latitude=pdata.get('gpsLatitude'),
                        gps_longitude=pdata.get('gpsLongitude'),
                        danger_signs=danger_signs,
                        overall_risk=pdata.get('overallRisk', 'LOW'),
                        suggestion_source=pdata.get('suggestionSource', 'LOCAL_RULES')
                    )

                synced_count += 1
            except Exception as e:
                print(f"Sync item error: {e}")

        # 2. Download Delta (Recent 20 Patients for this Facility)
        facility_patients = Patient.objects.filter(facility_name=facility_name).order_by('-updated_at')[:20] if facility_name else Patient.objects.all().order_by('-updated_at')[:20]
        download_serializer = PatientSerializer(facility_patients, many=True)

        return Response({
            'status': 'success',
            'syncedCount': synced_count,
            'downloadedPatients': download_serializer.data,
            'message': f'Successfully synced {synced_count} patient records.'
        }, status=status.HTTP_200_OK)
